
#include <jni.h>
#include <android/log.h>

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

//int decode_audio_file(const char* path, const int sample_rate, double** data, int* size) {
int decode_audio_file(const char* path, const int sample_rate) {

    FILE *clf = fopen("/storage/emulated/0/Music/audio_data.txt", "w+");
    fclose(clf);

    FILE *out = fopen("/storage/emulated/0/Music/audio_data.txt", "a+");

    // initialize all muxers, demuxers and protocols for libavformat
    // (does nothing if called twice during the course of one program execution)
    av_register_all();

    // get format from audio file
    AVFormatContext* format = avformat_alloc_context();
    if (avformat_open_input(&format, path, NULL, NULL) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not open file '%s'\n", path);
        return -1;
    }
    if (avformat_find_stream_info(format, NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not retrieve stream info from file '%s'\n", path);
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
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Could not retrieve audio stream from file '%s'\n", path);
        return -1;
    }
    AVStream* stream = format->streams[stream_index];

    // find & open codec
    AVCodecContext* codec = stream->codec;
    if (avcodec_open2(codec, avcodec_find_decoder(codec->codec_id), NULL) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "AMPLITUDA", "Failed to open decoder for stream #%u in file '%s'\n", stream_index, path);
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
    av_opt_set_int(swr, "out_sample_rate", sample_rate, 0);
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
            // rms
            float sum = 0;
            for(int i = 0; i < frame_count; i++) {
                for(int j = 0; j < frame->channels; j++) {
                    float sample = getSample(codec, frame->data[j], i);
                    sum += sample * sample;
                }
            }
            fprintf(out, "%d\n", ((int)(sqrt(sum / frame_count) * 10000)));
        } else {
            // amplitude
            int high_peak = 0, low_peak = 0;
            for(int i = 0; i < frame_count; i++) {
                if(frame->data[0][i] > high_peak) {
                    high_peak = frame->data[0][i];
                }

                if(frame->data[0][i] < low_peak) {
                    low_peak = frame->data[0][i];
                }
            }
            fprintf(out, "%d\n", (high_peak - low_peak - 128));
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


extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
) {

    // decode data
    int sample_rate = 44100;
//    double* data;
//    int size;

    const char* path = env->GetStringUTFChars(audio_path, 0);

//    if (decode_audio_file(path, sample_rate, &data, &size) != 0) {
    if (decode_audio_file(path, sample_rate) != 0) {
        return env->NewStringUTF("");
    }

//    free(data);

    env->ReleaseStringUTFChars(audio_path, path);

    return env->NewStringUTF("");
}
