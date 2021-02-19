#include <jni.h>
#include <android/log.h>
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

int process_audio(const char* input_audio, const char* temp_audio) {
    int result_code = ORIGINAL_INPUT;

    // initialize all muxers, demuxers and protocols for libavformat
    av_register_all();

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

//int decode_audio_file(const char* path, const int sample_rate, double** data, int* size) {
void decode_audio_file(
        const char* input_audio,
        const char* temp_txt
) {
    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "INPUT = %s", input_audio);

    // clear prev data
    FILE *clf = fopen(temp_txt, "w+");
    fclose(clf);
    // reopen
    FILE *out = fopen(temp_txt, "a+");

    // initialize all muxers, demuxers and protocols for libavformat
    // (does nothing if called twice during the course of one program execution)
    av_register_all();

    AVFormatContext *format = provideFormat(input_audio);
    AVStream *stream = provideStream(format);
    AVCodecContext *codec = provideCodec(stream);

    // When codec equals to NULL, it means that prev data: stream and format are also NULL
    if(codec == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to init data from file %s\n", input_audio);
        return;
    }

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

    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "%s", codec->codec->name);
    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "channels = %d", codec->channels);

    while (av_read_frame(format, &packet) >= 0) {
        // decode one frame
        int gotFrame;
        if (avcodec_decode_audio4(codec, frame, &gotFrame, &packet) < 0) {
            break;
        }
        if (!gotFrame) {
            continue;
        }
        // resample frames
        double* buffer;
        av_samples_alloc((uint8_t**) &buffer, NULL, 1, frame->nb_samples, AV_SAMPLE_FMT_DBL, 0);
        int frame_count = swr_convert(swr, (uint8_t**) &buffer, frame->nb_samples, (const uint8_t**) frame->data, frame->nb_samples);

        if(is_not_wav) {
            // planar
            float sum = 0;
            for(int i = 0; i < frame_count; i++) {
                for(int j = 0; j < frame->channels; j++) {
                    float sample = getSample(codec, frame->data[j], i);
                    sum += sample * sample;
                }
            }
            fprintf(out, "%d\n", ((int)(sqrt(sum / frame_count) * 100)));
        } else {
            float sum = 0;
            for(int i = 0; i < frame_count; i++) {
                float sample = getSample(codec, frame->data[0], i);
                sum += sample * sample;
            }
            fprintf(out, "%d\n", ((int)(sqrt(sum / frame_count) * 100)));
        }

    }

    // clean up
    av_frame_free(&frame);
    swr_free(&swr);
    avcodec_close(codec);
    avformat_free_context(format);
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

    int processed_result = process_audio(input_audio, temp_audio);

    if(processed_result == ORIGINAL_INPUT) {
        decode_audio_file(input_audio, temp_txt);
    } else if(processed_result == CONVERTED_INPUT) {
        decode_audio_file(temp_audio, temp_txt);
    }

    env->ReleaseStringUTFChars(audio_path, input_audio);
    env->ReleaseStringUTFChars(txt_cache, temp_txt);
    env->ReleaseStringUTFChars(audio_cache, temp_audio);
    return 0;
}


/*
int decode_audio_file(
        const char* input_audio,
        const char* temp_txt,
        const char* temp_audio
) {

    // clear prev data
    FILE *clf = fopen(temp_txt, "w+");
    fclose(clf);
    // reopen
    FILE *out = fopen(temp_txt, "a+");

    // initialize all muxers, demuxers and protocols for libavformat
    // (does nothing if called twice during the course of one program execution)
    av_register_all();

    // get format from audio file
    AVFormatContext* format = avformat_alloc_context();
    if (avformat_open_input(&format, input_audio, NULL, NULL) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not open file '%s'. Check READ and WRITE permissions.\n", input_audio);
        return -1;
    }
    if (avformat_find_stream_info(format, NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not retrieve stream info from file '%s'\n", input_audio);
        return -1;
    }

    // Find the index of the first audio stream
    int stream_index =- 1;
    for (int i=0; i<format->nb_streams; i++) {
        if (format->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            stream_index = i;
            break;
        }
    }
    if (stream_index == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not retrieve audio stream from file '%s'\n", input_audio);
        return -1;
    }
    AVStream* stream = format->streams[stream_index];

    // find & open codec
    AVCodecContext* codec = stream->codec;
    if (avcodec_open2(codec, avcodec_find_decoder(codec->codec_id), NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to open decoder for stream #%u in file '%s'\n", stream_index, input_audio);
        return -1;
    }

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
        return -1l;
    }

    // prepare to read data
    AVPacket packet;
    av_init_packet(&packet);
    AVFrame* frame = av_frame_alloc();
    if (!frame) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Error allocating the frame");
        return -1;
    }

    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "%s", codec->codec->name);
    __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "channels = %d", codec->channels);


    while (av_read_frame(format, &packet) >= 0) {
        // decode one frame
        int gotFrame;
        if (avcodec_decode_audio4(codec, frame, &gotFrame, &packet) < 0) {
            break;
        }
        if (!gotFrame) {
            continue;
        }
        // resample frames
        double* buffer;
        av_samples_alloc((uint8_t**) &buffer, NULL, 1, frame->nb_samples, AV_SAMPLE_FMT_DBL, 0);
        int frame_count = swr_convert(swr, (uint8_t**) &buffer, frame->nb_samples, (const uint8_t**) frame->data, frame->nb_samples);

        if(is_not_wav) {
            // planar
            float sum = 0;
            for(int i = 0; i < frame_count; i++) {
                for(int j = 0; j < frame->channels; j++) {
                    float sample = getSample(codec, frame->data[j], i);
                    sum += sample * sample;
                }
            }
            fprintf(out, "%d\n", ((int)(sqrt(sum / frame_count) * 1000)));
        } else {
            if(strcmp(codec->codec->name, "pcm_u8") == 0) {
                // pcm_u8
                float sum = 0;
                for(int i = 0; i < frame_count; i++) {
                    float sample = getSample(codec, frame->data[0], i);
                    sum += sample * sample;
                }
                fprintf(out, "%d\n", ((int)(sqrt(sum / frame_count) * 1000)));
            } else {


            }


        }

    }

    // clean up
    av_frame_free(&frame);
    swr_free(&swr);
    avcodec_close(codec);
    avformat_free_context(format);

    fclose(out);
    // success
    return 0;

}

*/