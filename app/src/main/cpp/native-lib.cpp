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

static int decode_packet(AVCodecContext *dec, const AVPacket *pkt)
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

    int ret = 0;

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
        audio_dst_file = fopen(temp_txt, "a");
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
            ret = decode_packet(audio_dec_ctx, pkt);
        }

        av_packet_unref(pkt);
        if (ret < 0)
            break;
    }

    // flush the decoders
    if (audio_dec_ctx)
        decode_packet(audio_dec_ctx, NULL);

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
}