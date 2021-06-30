#include <jni.h>
#include <android/log.h>

extern "C" {
#include "libavutil/timestamp.h"
#include "libavutil/samplefmt.h"
#include "libavformat/avformat.h"
}

static AVFormatContext *fmt_ctx = NULL;
static AVCodecContext *audio_dec_ctx;
static AVStream *audio_stream = NULL;
static const char *src_filename = NULL;
static FILE *audio_dst_file = NULL;

static int audio_stream_idx = -1;
static AVFrame *frame = NULL;
static AVPacket *pkt = NULL;

double getSample(const AVCodecContext* codecCtx, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    double ret = 0;
    int sampleSize = av_get_bytes_per_sample(codecCtx->sample_fmt);

    switch(sampleSize) {
        case 1:
            val = (reinterpret_cast<uint8_t*>(buffer))[sampleIndex];
            val -= 127;
            break;
        case 2:
            val = (reinterpret_cast<int16_t*>(buffer))[sampleIndex];
            break;
        case 4: val = (reinterpret_cast<uint32_t*>(buffer))[sampleIndex];
            break;
        case 8: val = (reinterpret_cast<uint64_t*>(buffer))[sampleIndex];
            break;
        default:
            return 0;
    }

    // Check which data type is in the sample.
    switch(codecCtx->sample_fmt) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_U8P:
        case AV_SAMPLE_FMT_S16P:
        case AV_SAMPLE_FMT_S32P:
            // integer => Scale to [-1, 1] and convert to float.
            ret = val / (static_cast<float>(((1 << (sampleSize*8-1))-1)));
            break;
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            // float => reinterpret
            ret = *reinterpret_cast<float*>(&val);
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
            // double => reinterpret and then static cast down
            ret = *reinterpret_cast<double*>(&val);
            break;
        default:
            return 0;
    }

    return ret;

}

static int decode_packet(AVCodecContext *dec, const AVPacket *pkt, const int* is_planar, const int* is_pcm_s16)
{
    int ret = 0;

    // submit the packet to the decoder
    ret = avcodec_send_packet(dec, pkt);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error submitting a packet for decoding (%s)\n", av_err2str(ret));
        return ret;
    }

    // get all the available frames from the decoder
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec, frame);
        if (ret < 0) {
            // those two return values are special and mean there is no output
            // frame available, but there were no errors during decoding
            if (ret == AVERROR_EOF || ret == AVERROR(EAGAIN)) {
//                __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "1st if for Total frames = %d\n", frames);
                return 0;
            }

            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error during decoding (%s)\n", av_err2str(ret));
            return ret;
        }

        // write the frame data to output file
        if(dec->codec->type == AVMEDIA_TYPE_AUDIO) {
            double sum = 0;


            for(int i = 0; i < frame->nb_samples; i++) {
                double sample = getSample(audio_dec_ctx, frame->data[0], i);
                sum += sample * sample;
            }
//            if(*is_pcm_s16) { // mp3
//                 for (c = 0; c < data->channels; ++c)
//                for(int i = 0; i < frame->nb_samples; i++) {
//                    double sample = getSample(audio_dec_ctx, frame->data[0], i);
//                    sum += sample * sample;
//                }
//            } else {
//
//            }

            /*if(*is_planar) { // is not wav
                // for (c = 0; c < data->channels; ++c)

                for(int i = 0; i < frame->nb_samples; i += frame->channels) {
                    float sample = getSample(audio_dec_ctx, frame->data[0], i);
                    sum += sample * sample;
                }

                 for(int i = 0; i < frame->nb_samples; i++) {
                    double sample = getSample(audio_dec_ctx, frame->data[0], i);
                     __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Mp3 = %f\n", sample);
                     sum += sample * sample;
                 }
            } else {
                //for (i = c; i < samples_per_pixel; i += data->channels)
                if(*is_pcm_s16) {
                    for(int i = 0; i < frame->nb_samples; i++) {
//                        double sample = getSample(audio_dec_ctx, frame->data[0], i);
//                        sum += sample * sample;

                        for(int j = 0; j < frame->channels; j++) {
                            double sample = getSample(audio_dec_ctx, frame->data[j], i);
                            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Wav s16 = %f\n", sample);
                            sum += sample * sample;
                        }
                    }
                } else {
                    for(int i = 0; i < frame->nb_samples; i++) {
                        for(int j = 0; j < frame->channels; j++) {
                            double sample = getSample(audio_dec_ctx, frame->data[j], i);
                            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Wav u8 = %f\n", sample);
                            sum += sample * sample;
                        }
                    }
                }
            }*/


            fprintf(audio_dst_file, "%d\n", ((int)(sqrt(sum / frame->nb_samples) * 100)));
//            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "PLANAR = %d || S16 = %d\n", *is_planar, *is_pcm_s16);

        }

        av_frame_unref(frame);
        if (ret < 0) {
//            __android_log_print(ANDROID_LOG_ERROR, "2nd if for AMPLITUDA", "Total frames = %d\n", frames);
            return ret;
        }
    }

//    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "last if for Total frames = %d\n", frames);

    return 0;
}

static int open_codec_context(
        int *stream_idx,
        AVCodecContext **dec_ctx,
        AVFormatContext *fmt_ctx,
        enum AVMediaType type
) {
    int ret, stream_index;
    AVStream *st;
    const AVCodec *dec = NULL;
    AVDictionary *opts = NULL;

    ret = av_find_best_stream(fmt_ctx, type, -1, -1, NULL, 0);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not find %s stream in input file '%s'\n", av_get_media_type_string(type), src_filename);
        return ret;
    } else {
        stream_index = ret;
        st = fmt_ctx->streams[stream_index];

        /* find decoder for the stream */
        dec = avcodec_find_decoder(st->codecpar->codec_id);
        if (!dec) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to find %s codec\n", av_get_media_type_string(type));
            return AVERROR(EINVAL);
        }

        /* Allocate a codec context for the decoder */
        *dec_ctx = avcodec_alloc_context3(dec);
        if (!*dec_ctx) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to allocate the %s codec context\n",
                    av_get_media_type_string(type));
            return AVERROR(ENOMEM);
        }

        /* Copy codec parameters from input stream to output codec context */
        if ((ret = avcodec_parameters_to_context(*dec_ctx, st->codecpar)) < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to copy %s codec parameters to decoder context\n",
                    av_get_media_type_string(type));
            return ret;
        }

        /* Init the decoders */
        if ((ret = avcodec_open2(*dec_ctx, dec, &opts)) < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to open %s codec\n",
                    av_get_media_type_string(type));
            return ret;
        }
        *stream_idx = stream_index;
    }

    return 0;
}

static int get_format_from_sample_fmt(const char **fmt,
                                      enum AVSampleFormat sample_fmt)
{
    int i;
    struct sample_fmt_entry {
        enum AVSampleFormat sample_fmt; const char *fmt_be, *fmt_le;
    } sample_fmt_entries[] = {
            { AV_SAMPLE_FMT_U8,  "u8",    "u8"    },
            { AV_SAMPLE_FMT_S16, "s16be", "s16le" },
            { AV_SAMPLE_FMT_S32, "s32be", "s32le" },
            { AV_SAMPLE_FMT_FLT, "f32be", "f32le" },
            { AV_SAMPLE_FMT_DBL, "f64be", "f64le" },
    };
    *fmt = NULL;

    for (i = 0; i < FF_ARRAY_ELEMS(sample_fmt_entries); i++) {
        struct sample_fmt_entry *entry = &sample_fmt_entries[i];
        if (sample_fmt == entry->sample_fmt) {
            *fmt = AV_NE(entry->fmt_be, entry->fmt_le);
            return 0;
        }
    }

    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "sample format %s is not supported as output format\n",
            av_get_sample_fmt_name(sample_fmt));
    return -1;
}

extern "C" JNIEXPORT jint JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path,
        jstring txt_cache,
        jstring audio_cache
) {

//    /storage/emulated/0/Music/ncs_hr.mp3
//    /storage/emulated/0/Music/dwv.mp4
//    /storage/emulated/0/Music/kygo_s16.wav
//    /storage/emulated/0/Music/kygo_u8.wav
//    /storage/emulated/0/Music/igor.wav
//    /storage/emulated/0/Music/kygo.mp3
//    const char* temp_txt = env->GetStringUTFChars(txt_cache, 0);
//    wav2mp3("/storage/emulated/0/Music/kygo_s16.wav", "/storage/emulated/0/Music/converted.mp3");
//    wav2mp3("/storage/emulated/0/Music/igor.wav", temp_txt);

    int ret = 0;
    int is_planar = 0;
    int is_pcm_s16 = 0;

    const char* temp_txt = env->GetStringUTFChars(txt_cache, 0);
    const char* temp_audio = env->GetStringUTFChars(audio_cache, 0);
    // Use converted file instead of input (only when converted flag - true).
    const char* input_audio = env->GetStringUTFChars(audio_path, 0);


    // open input file, and allocate format context
    if (avformat_open_input(&fmt_ctx, input_audio, NULL, NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not open source file %s\n", input_audio);
        return -1;
    }

    // retrieve stream information
    if (avformat_find_stream_info(fmt_ctx, NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not find stream information\n");
        return -1;
    }

    if (open_codec_context(&audio_stream_idx, &audio_dec_ctx, fmt_ctx, AVMEDIA_TYPE_AUDIO) >= 0) {
        audio_stream = fmt_ctx->streams[audio_stream_idx];
        audio_dst_file = fopen(temp_txt, "a+");
        is_planar = av_sample_fmt_is_planar(audio_dec_ctx->sample_fmt);

        is_pcm_s16 = strcmp(audio_dec_ctx->codec->name, "pcm_u8") != 0;
    }

    // dump input information to stderr
    av_dump_format(fmt_ctx, 0, input_audio, 0);

    if (!audio_stream) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not find audio or video stream in the input, aborting\n");
        ret = 1;
        goto end;
    }

    frame = av_frame_alloc();
    if (!frame) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not allocate frame\n");
        ret = AVERROR(ENOMEM);
        goto end;
    }

    pkt = av_packet_alloc();
    if (!pkt) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not allocate packet\n");
        ret = AVERROR(ENOMEM);
        goto end;
    }

    // read frames from the file
    while (av_read_frame(fmt_ctx, pkt) >= 0) {
        // check if the packet belongs to a stream we are interested in, otherwise skip it
        if (pkt->stream_index == audio_stream_idx) {
            ret = decode_packet(audio_dec_ctx, pkt, &is_planar, &is_pcm_s16);
        }

        av_packet_unref(pkt);
        if (ret < 0)
            break;
    }

    // flush the decoders
    if (audio_dec_ctx)
        decode_packet(audio_dec_ctx, NULL, &is_planar, &is_pcm_s16);

    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Success!\n");

    if (audio_stream) {
        enum AVSampleFormat sfmt = audio_dec_ctx->sample_fmt;
        int n_channels = audio_dec_ctx->channels;
        const char *fmt;

        if (av_sample_fmt_is_planar(sfmt)) {
            const char *packed = av_get_sample_fmt_name(sfmt);

            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Warning: the sample format the decoder produced is planar (%s). This example will output the first channel only.\n", packed ? packed : "?");

            sfmt = av_get_packed_sample_fmt(sfmt);
            n_channels = 1;
        }

        if ((ret = get_format_from_sample_fmt(&fmt, sfmt)) < 0)
            goto end;
    }

    end:
    avcodec_free_context(&audio_dec_ctx);
    avformat_close_input(&fmt_ctx);
    if (audio_dst_file)
        fclose(audio_dst_file);
    av_packet_free(&pkt);
    av_frame_free(&frame);

    env->ReleaseStringUTFChars(audio_path, input_audio);
    env->ReleaseStringUTFChars(txt_cache, temp_txt);
    env->ReleaseStringUTFChars(audio_cache, temp_audio);

    return ret < 0;
//    env->ReleaseStringUTFChars(txt_cache, temp_txt);
//    return 0;
}

/*extern "C" {

#include "libavutil/frame.h"
#include "libavutil/mem.h"

#include "libavcodec/avcodec.h"
}
#define AUDIO_INBUF_SIZE 20480
#define AUDIO_REFILL_THRESH 4096

static void decode(AVCodecContext *dec_ctx, AVPacket *pkt, AVFrame *frame,
                   FILE *outfile)
{
    int i, ch;
    int ret, data_size;

    ret = avcodec_send_packet(dec_ctx, pkt);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error submitting the packet to the decoder\n");
        return;
    }

    while (ret >= 0) {
        ret = avcodec_receive_frame(dec_ctx, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            return;
        else if (ret < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error during decoding\n");
            return;
        }
        data_size = av_get_bytes_per_sample(dec_ctx->sample_fmt);
        if (data_size < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to calculate data size\n");
            return;
        }
        for (i = 0; i < frame->nb_samples; i++) {
            for (ch = 0; ch < dec_ctx->channels; ch++) {
                fwrite(frame->data[ch] + data_size*i, 1, data_size, outfile);
            }
        }
    }
}

extern "C" JNIEXPORT jint JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path,
        jstring txt_cache,
        jstring audio_cache
) {
    const char *outfilename, *filename;
    const AVCodec *codec;
    AVCodecContext *c= NULL;
    AVCodecParserContext *parser = NULL;
    int len, ret;
    FILE *f, *outfile;
    uint8_t inbuf[AUDIO_INBUF_SIZE + AV_INPUT_BUFFER_PADDING_SIZE];
    uint8_t *data;
    size_t   data_size;
    AVPacket *pkt;
    AVFrame *decoded_frame = NULL;

    filename    = "/storage/emulated/0/Music/kygo.mp3";
    outfilename = "/storage/emulated/0/Music/decoded.mp3";

    pkt = av_packet_alloc();

    codec = avcodec_find_decoder(AV_CODEC_ID_MP3);
    if (!codec) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Codec not found\n");
        return 0;
    }

    parser = av_parser_init(codec->id);
    if (!parser) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Parser not found\n");
        return 0;

    }

    c = avcodec_alloc_context3(codec);
    if (!c) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not allocate audio codec context\n");
        return 0;
    }

    if (avcodec_open2(c, codec, NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not open codec\n");
        return 0;
    }

    f = fopen(filename, "rb");
    if (!f) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not open %s\n", filename);
        return 0;
    }
    outfile = fopen(outfilename, "wb");
    if (!outfile) {
        av_free(c);
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Outfile error %s\n", filename);
        return 0;
//        exit(1);
    }

    data      = inbuf;
    data_size = fread(inbuf, 1, AUDIO_INBUF_SIZE, f);

    while (data_size > 0) {
        if (!decoded_frame) {
            if (!(decoded_frame = av_frame_alloc())) {
                __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not allocate audio frame\n");
                return 0;
            }
        }

        ret = av_parser_parse2(parser, c, &pkt->data, &pkt->size,
                               data, data_size,
                               AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
        if (ret < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error while parsing\n");
            return 0;
        }
        data      += ret;
        data_size -= ret;

        if (pkt->size)
            decode(c, pkt, decoded_frame, outfile);

        if (data_size < AUDIO_REFILL_THRESH) {
            memmove(inbuf, data, data_size);
            data = inbuf;
            len = fread(data + data_size, 1,
                        AUDIO_INBUF_SIZE - data_size, f);
            if (len > 0)
                data_size += len;
        }
    }

    pkt->data = NULL;
    pkt->size = 0;
    decode(c, pkt, decoded_frame, outfile);

    fclose(outfile);
    fclose(f);

    avcodec_free_context(&c);
    av_parser_close(parser);
    av_frame_free(&decoded_frame);
    av_packet_free(&pkt);

    return 0;
}*/

/*
#include "lame/include/lame.h"
#define SAMPLE_RATE 44100

#define ORIGINAL_INPUT 11
#define CONVERTED_INPUT 12
#define INVALID_INPUT 13

extern "C" {
#include "libavutil/opt.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"

}

float getSample(const AVCodecContext* codecCtx, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    float ret = 0;
    int sampleSize = av_get_bytes_per_sample(codecCtx->sample_fmt);

    switch(sampleSize) {
        case 1:
            val = (reinterpret_cast<uint8_t*>(buffer))[sampleIndex];
            val -= 127;
            break;
        case 2: val = (reinterpret_cast<uint16_t*>(buffer))[sampleIndex];
            break;
        case 4: val = (reinterpret_cast<uint32_t*>(buffer))[sampleIndex];
            break;
        case 8: val = (reinterpret_cast<uint64_t*>(buffer))[sampleIndex];
            break;
        default:
            return 0;
    }

    // Check which data type is in the sample.
    switch(codecCtx->sample_fmt) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_U8P:
        case AV_SAMPLE_FMT_S16P:
        case AV_SAMPLE_FMT_S32P:
            // integer => Scale to [-1, 1] and convert to float.
            ret = val / (static_cast<float>(((1 << (sampleSize*8-1))-1)));
            break;
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            // float => reinterpret
            ret = *reinterpret_cast<float*>(&val);
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
            // double => reinterpret and then static cast down
            ret = static_cast<float>(*reinterpret_cast<double*>(&val));
            break;
        default:
            return 0;
    }

    return ret;

}

AVFormatContext* provideFormat(const char *input_audio) {
    // get format from audio file
    AVFormatContext *format = avformat_alloc_context();
    if (avformat_open_input(&format, input_audio, NULL, NULL) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not open file '%s'. Check READ and WRITE permissions.\n", input_audio);
        return NULL;
    }
    return format;
}

AVStream* provideStream(AVFormatContext *format) {
    // Init stream from format
    if (avformat_find_stream_info(format, NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not retrieve stream info from input audio file\n");
        return NULL;
    }
    // Find the index of the first audio stream
    int stream_index =- 1;

    __android_log_print(ANDROID_LOG_INFO, "AMPLITUDA", "Number of streams = %d\n", format->nb_streams);

    for (int i=0; i<format->nb_streams; i++) {
        if (format->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            stream_index = i;
            break;
        }
    }
    if (stream_index == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not retrieve audio stream from input audio file \n");
        return NULL;
    }
    return format->streams[stream_index];
}

AVCodecContext* provideCodec(AVStream *stream) {
    // find & open codec
    AVCodecContext *codec = stream->codec;
    if (avcodec_open2(codec, avcodec_find_decoder(codec->codec_id), NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to open decoder for stream #%u in the input audio file\n", stream->index);
        return NULL;
    }
    return codec;
}

int prepare_audio(const char* input_audio, const char* temp_audio) {
    int result_code = ORIGINAL_INPUT;

    AVFormatContext *format = provideFormat(input_audio);
    AVStream *stream = provideStream(format);
    AVCodecContext *codec = provideCodec(stream);

    // When codec equals to NULL, it means that prev data: stream and format are also NULL
    if(codec == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to init data from file %s\n", input_audio);
        return INVALID_INPUT;
    }

    // If file is wav (planar) and codec is not pcm_u8
    if(!av_sample_fmt_is_planar(codec->sample_fmt) && strcmp(codec->codec->name, "pcm_u8") != 0) {
        int read, write;
        // Open input wav
        FILE *pcm = fopen(input_audio, "rb");
        // Skip wav header
        fseek(pcm, 4 * 1024, SEEK_CUR);

        // Open output temp mp3 file
        FILE *mp3 = fopen(temp_audio, "wb");
        const int PCM_SIZE = 8192 * 3;
        const int MP3_SIZE = 8192 * 3;
        short int pcm_buffer[PCM_SIZE * 2];
        unsigned char mp3_buffer[MP3_SIZE];
        // Prepare lame
        lame_t lame = lame_init();
        lame_set_in_samplerate(lame, SAMPLE_RATE);
        lame_set_VBR(lame, vbr_default);
        lame_init_params(lame);

        int nTotalRead=0;
        // Convert wav to mp3
        do {
            read = fread(pcm_buffer, 2*sizeof(short int), PCM_SIZE, pcm);
            nTotalRead+=read*4;

            if (read == 0) write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
            else write = lame_encode_buffer_interleaved(lame,pcm_buffer, read, mp3_buffer, MP3_SIZE);

            fwrite(mp3_buffer, write, 1, mp3);
        } while (read != 0);

        // clean up lame
        lame_close(lame);
        fclose(mp3);
        fclose(pcm);
        result_code = CONVERTED_INPUT;
    }

    // clean up
    avcodec_close(codec);
    avformat_free_context(format);

    return result_code;
}

void decode_audio_file(
        const char* input_audio,
        const char* temp_txt
) {

    int tmp_file_index = 0;

    FILE *out = fopen(temp_txt, "a+");

    // initialize all muxers, demuxers and protocols for libavformat
    // (does nothing if called twice during the course of one program execution)
//    av_register_all();

    AVFormatContext *format = provideFormat(input_audio);
    AVStream *stream = provideStream(format);
    AVCodecContext *codec = provideCodec(stream);

    // When codec equals to NULL, it means that prev data: stream and format are also NULL
    if(codec == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to init data from file %s\n", input_audio);
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, "AMPLITUDA", "Audio codec found = %s \n", (codec->codec_type == AVMEDIA_TYPE_AUDIO ? "true" : "false"));

    int is_not_wav = av_sample_fmt_is_planar(codec->sample_fmt);

    // prepare resampler
    struct SwrContext* swr = swr_alloc();
    av_opt_set_int(swr, "in_channel_count",  codec->channels, 0);
    av_opt_set_int(swr, "out_channel_count", 1, 0);
    av_opt_set_int(swr, "in_channel_layout",  codec->channel_layout, 0);
    av_opt_set_int(swr, "out_channel_layout", AV_CH_LAYOUT_MONO, 0);
    av_opt_set_int(swr, "in_sample_rate", codec->sample_rate, 0);
    av_opt_set_int(swr, "out_sample_rate", SAMPLE_RATE, 0);
    av_opt_set_sample_fmt(swr, "in_sample_fmt",  codec->sample_fmt, 0);
    av_opt_set_sample_fmt(swr, "out_sample_fmt", AV_SAMPLE_FMT_DBL,  0);
    swr_init(swr);
    if (!swr_is_initialized(swr)) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Resampler has not been properly initialized");
        return;
    }

    // prepare to read data
    AVPacket packet;
    av_init_packet(&packet);
    AVFrame* frame = av_frame_alloc();
    if (!frame) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error allocating the frame");
        return;
    }

    int current_frame = 0;

    while (av_read_frame(format, &packet) >= 0) {
        // Swap tmp files
        */
/*if(current_frame != 0 && current_frame % 30000 == 0) {
            char path[55];
            tmp_file_index++;
            sprintf(path, "/storage/emulated/0/Music/amplituda_tmp_text_%d.txt", tmp_file_index);
            fflush(out);
            fclose(out);
            out = fopen(path, "a+");
            __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Open next => %s", path);
        }*//*


        current_frame++;

        // decode one frame
        int gotFrame;

        if (avcodec_decode_audio4(codec, frame, &gotFrame, &packet) < 0) {
            __android_log_print(ANDROID_LOG_INFO, "AMPLITUDA", "Skip invalid frame");
            continue;
        }
        if (!gotFrame) {
            continue;
        }


        // resample frames
//        double* buffer;
        uint8_t **buffer;

//        av_samples_alloc(buffer, NULL, 1, frame->nb_samples, AV_SAMPLE_FMT_DBL, 0);
//        int frame_count = swr_convert(swr, buffer, frame->nb_samples, (const uint8_t**) frame->data, frame->nb_samples);

        av_samples_alloc((uint8_t**) &buffer, NULL, 1, frame->nb_samples, AV_SAMPLE_FMT_DBL, 0);
        int frame_count = swr_convert(swr, (uint8_t**) &buffer, frame->nb_samples, (const uint8_t**) frame->data, frame->nb_samples);

        float sum = 0;
        if(is_not_wav) {
            // planar
            for(int i = 0; i < frame_count; i++) {
                for(int j = 0; j < frame->channels; j++) {
                    float sample = getSample(codec, frame->data[j], i);
                    sum += sample * sample;
                }
            }
        } else {
            for(int i = 0; i < frame_count; i++) {
                float sample = getSample(codec, frame->data[0], i);
                sum += sample * sample;
            }
        }
        fprintf(out, "%d\n", ((int)(sqrt(sum / frame_count) * 100)));

    }

    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Frames = %d", current_frame);


    // clean up
    av_frame_free(&frame);
    swr_free(&swr);
    avcodec_close(codec);
    avformat_free_context(format);

    // close output tmp
    fflush(out);
    fclose(out);
}

extern "C" JNIEXPORT jint JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path,
        jstring txt_cache,
        jstring audio_cache
) {
    const char* input_audio = env->GetStringUTFChars(audio_path, 0);
    const char* temp_txt = env->GetStringUTFChars(txt_cache, 0);
    const char* temp_audio = env->GetStringUTFChars(audio_cache, 0);

    // initialize all muxers, demuxers and protocols for libavformat
    av_register_all();

    int processed_result = prepare_audio(input_audio, temp_audio);

//    if(processed_result == ORIGINAL_INPUT) {
        decode_audio_file(input_audio, temp_txt);
//    } else if(processed_result == CONVERTED_INPUT) {
//        decode_audio_file(temp_audio, temp_txt);
//    }

    env->ReleaseStringUTFChars(audio_path, input_audio);
    env->ReleaseStringUTFChars(txt_cache, temp_txt);
    env->ReleaseStringUTFChars(audio_cache, temp_audio);

    return 0;
}*/
