
#include <jni.h>
#include <android/log.h>

extern "C" {
#include "libavutil/opt.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
};

int decode_audio_file(const char* path, const int sample_rate, double** data, int* size) {

    // initialize all muxers, demuxers and protocols for libavformat
    // (does nothing if called twice during the course of one program execution)
    av_register_all();

    // get format from audio file
    AVFormatContext* format = avformat_alloc_context();
    if (avformat_open_input(&format, path, NULL, NULL) != 0) {
        fprintf(stderr, "Could not open file '%s'\n", path);
        return -1;
    }
    if (avformat_find_stream_info(format, NULL) < 0) {
        fprintf(stderr, "Could not retrieve stream info from file '%s'\n", path);
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
        fprintf(stderr, "Could not retrieve audio stream from file '%s'\n", path);
        return -1;
    }
    AVStream* stream = format->streams[stream_index];

    // find & open codec
    AVCodecContext* codec = stream->codec;
    if (avcodec_open2(codec, avcodec_find_decoder(codec->codec_id), NULL) < 0) {
        fprintf(stderr, "Failed to open decoder for stream #%u in file '%s'\n", stream_index, path);
        return -1;
    }

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
        fprintf(stderr, "Resampler has not been properly initialized\n");
        return -1;
    }

    // prepare to read data
    AVPacket packet;
    av_init_packet(&packet);
    AVFrame* frame = av_frame_alloc();
    if (!frame) {
        fprintf(stderr, "Error allocating the frame\n");
        return -1;
    }

    // iterate through frames
    *data = NULL;
    *size = 0;
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
        int frame_count = swr_convert(swr, (uint8_t**) &buffer, frame->nb_samples, (uint8_t**) frame->data, frame->nb_samples);
        // append resampled frames to data
        *data = (double*) realloc(*data, (*size + frame->nb_samples) * sizeof(double));
        memcpy(*data + *size, buffer, frame_count * sizeof(double));
        *size += frame_count;
    }

    // clean up
    av_frame_free(&frame);
    swr_free(&swr);
    avcodec_close(codec);
    avformat_free_context(format);

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
    double* data;
    int size;
    if (decode_audio_file("/storage/emulated/0/Music/clap_effect.mp3", sample_rate, &data, &size) != 0) {
//    if (decode_audio_file("/storage/9016-4EF8/MUSIC/Kygo - Broken Glass.mp3", sample_rate, &data, &size) != 0) {
//    if (decode_audio_file("/storage/9016-4EF8/MUSIC/Worakls - Red Dressed (Ben Böhmer Remix).mp3", sample_rate, &data, &size) != 0) {
//    if (decode_audio_file("/storage/emulated/0/Music/kygo.wav", sample_rate, &data, &size) != 0) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "FAIL");
        return env->NewStringUTF("");
    }

    FILE *clf = fopen("/storage/emulated/0/Music/audio_data.txt", "w+");
    fclose(clf);

    FILE *out = fopen("/storage/emulated/0/Music/audio_data.txt", "a+");


    // sum data
    // todo use as wav alternative

    double peak = 0;
    for (int i=0; i<size; ++i) { // i+= 1920

        if(peak < data[i]) {
            peak = data[i];
        }

        if(i % 64 == 0) {
            double amp =  ( (peak >= 0 ? peak : (-peak)) );
            int sample = ((int)pow((((( amp * 2) / 4) * 100)), 0.5));

            fprintf(out, "%d\n", sample);
            peak = -2.0;
        }

    }

    /*for (int i=0; i<size; ++i) { // i+= 1920

        if(peak < data[i]) {
            peak = data[i];
        }

        if(i % 64 == 0) {
            fprintf(out, "%f\n", peak >= 0 ? peak : (-peak));
            peak = -2.0;
        }

    }*/
    /*
    for (int i=0; i<size; ++i) { // i+= 1920
        if(data[i] < 0)
            sum += -data[i];
        else
            sum += data[i];

        if(i % 96 == 0) {

//            int sample = ((int)pow((((( (data[i] + (-min)) * 2) / 4) * 100)), 0.5));
            if(data[i] > 0) {
                int sample = (int)pow(((sum / 2) * 100), 0.5);
                fprintf(out, "%d\n", sample );
            }

//            fprintf(out, "%d\n", ((int)((data[i]*50)/ave)) );
//            fprintf(out, "%d\n", ((int) (data[i] + (-min))) );
            sum = 0;
        }
    }*/

//    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "max = %f ::::: min = %f\n", max, min);

    fclose(out);
    free(data);

    return env->NewStringUTF("");
}

/*

#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
#include "libavutil/audio_fifo.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

std::string ConvertJString(JNIEnv* env, jstring str){
    if ( !str ) std::string();
    const jsize len = env->GetStringUTFLength(str);
    const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);
    std::string Result(strChars, len);
    env->ReleaseStringUTFChars(str, strChars);
    return Result;
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

extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
        ) {

    std::string filename = ConvertJString( env, audio_path );
    std::string resultFrame;

    av_register_all();

    AVFrame* frame = av_frame_alloc();

    if (!frame){
        return env->NewStringUTF("Error allocating the frame! Amplituda:exception:600");
    }

    AVFormatContext* formatContext = NULL;
    if (avformat_open_input(&formatContext, filename.data(), NULL, NULL) != 0) {
        av_free(frame);
        // FIXME: error with same file form waveformSeekBar
        return env->NewStringUTF("Error opening file, file path is not valid! Amplituda:exception:800");
    }

    if (avformat_find_stream_info(formatContext, NULL) < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Error finding the stream information! Amplituda:exception:57005");
    }

    AVCodec* cdc = nullptr;
    int streamIndex = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &cdc, 0);
    if (streamIndex < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Could not find any audio stream in the file! Amplituda:exception:165");
    }


    AVStream* audioStream = formatContext->streams[streamIndex];
    AVCodecContext* codecContext = audioStream->codec;
    codecContext->codec = cdc;


    if (avcodec_open2(codecContext, codecContext->codec, NULL) != 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Couldn't open the context with the decoder! (can't find or open context) Amplituda:exception:1057");
    }

    AVPacket readingPacket;
    av_init_packet(&readingPacket);

//    readingPacket.size

//    AVAudioFifo *fifo = av_audio_fifo_alloc(codecContext->sample_fmt, codecContext->channels, audioStream->);


    int is_wav = av_sample_fmt_is_planar(codecContext->sample_fmt);


    FILE *clf = fopen("/storage/emulated/0/Music/audio_data.txt", "w+");
    fclose(clf);

    FILE *out = fopen("/storage/emulated/0/Music/audio_data.txt", "a+");

    while (av_read_frame(formatContext, &readingPacket) == 0)
    {
        if (readingPacket.stream_index == audioStream->index)
        {
            AVPacket decodingPacket = readingPacket;
            while (decodingPacket.size > 0)
            {
                int gotFrame = 0;
                int result = avcodec_decode_audio4(codecContext, frame, &gotFrame, &decodingPacket);

                if (result >= 0 && gotFrame)
                {

                    decodingPacket.size -= result;
                    decodingPacket.data += result;

                     if(is_wav) {
                         __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "IS NOT WAV\n");

//
//                        float sum = 0;
//                        for(int s = 0; s < 4; ++s) {
//                            for(int c = 0; c < codecContext->channels; ++c) {
//                                float sample = getSample(codecContext, frame->extended_data[c], s);
//                                if(sample < 0)
//                                    sum += -sample;
//                                else
//                                    sum += sample;
//                            }
//                        }
//    //                    float average_point = (sum * 2) / 4;
//
//    //                    int amplitude = pow(((int)(average_point * 100)), 0.5);
//                        int amplitude = pow(((int)(((sum * 2) / 4) * 100)), 0.5);
//
//                        resultFrame.append(std::to_string(  amplitude ));
//                        resultFrame.append("\n");

                     } else {

                         int sum = 0;
                         for(int i = 0; i < result; i++) {
                             sum += frame->extended_data[0][i];
                             if(i % 128 == 0) {
//                                 fprintf(out, "%d\n", frame->extended_data[0][i]);
                                 sum = 0;
                             }
//                             __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Sample = %d\n", frame->extended_data[0][i]);

                         }
                     }


                }
                else
                {
                    decodingPacket.size = 0;
                    decodingPacket.data = nullptr;
                }
            }
        }

        av_free_packet(&readingPacket);
    }


    av_free(frame);
    avcodec_close(codecContext);
    avformat_close_input(&formatContext);
    return env->NewStringUTF(resultFrame.data());
}

*/

/*
 #include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

std::string ConvertJString(JNIEnv* env, jstring str){
    if ( !str ) std::string();
    const jsize len = env->GetStringUTFLength(str);
    const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);
    std::string Result(strChars, len);
    env->ReleaseStringUTFChars(str, strChars);
    return Result;
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

extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
        ) {

    std::string filename = ConvertJString( env, audio_path );
    std::string resultFrame;

    av_register_all();

    AVFrame* frame = av_frame_alloc();

    if (!frame){
        return env->NewStringUTF("Error allocating the frame! Amplituda:exception:600");
    }

    AVFormatContext* formatContext = NULL;
    if (avformat_open_input(&formatContext, filename.data(), NULL, NULL) != 0) {
        av_free(frame);
        // FIXME: error with same file form waveformSeekBar
        return env->NewStringUTF("Error opening file, file path is not valid! Amplituda:exception:800");
    }

    if (avformat_find_stream_info(formatContext, NULL) < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Error finding the stream information! Amplituda:exception:57005");
    }

    AVCodec* cdc = nullptr;
    int streamIndex = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &cdc, 0);
    if (streamIndex < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Could not find any audio stream in the file! Amplituda:exception:165");
    }

//    AVAudioFifo *fifo = av_audio_fifo_alloc();

    AVStream* audioStream = formatContext->streams[streamIndex];
    AVCodecContext* codecContext = audioStream->codec;
    codecContext->codec = cdc;

    if (avcodec_open2(codecContext, codecContext->codec, NULL) != 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Couldn't open the context with the decoder! (can't find or open context) Amplituda:exception:1057");
    }

    AVPacket readingPacket;
    av_init_packet(&readingPacket);

    int is_wav = av_sample_fmt_is_planar(codecContext->sample_fmt);

    while (av_read_frame(formatContext, &readingPacket) == 0)
    {
        if (readingPacket.stream_index == audioStream->index)
        {
            AVPacket decodingPacket = readingPacket;
            while (decodingPacket.size > 0)
            {
                int gotFrame = 0;
                int result = avcodec_decode_audio4(codecContext, frame, &gotFrame, &decodingPacket);

                if (result >= 0 && gotFrame)
                {

                     if(is_wav) {
                         __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "IS NOT WAV\n");

//                        decodingPacket.size -= result;
//                        decodingPacket.data += result;
//
//                        float sum = 0;
//                        for(int s = 0; s < 4; ++s) {
//                            for(int c = 0; c < codecContext->channels; ++c) {
//                                float sample = getSample(codecContext, frame->extended_data[c], s);
//                                if(sample < 0)
//                                    sum += -sample;
//                                else
//                                    sum += sample;
//                            }
//                        }
//    //                    float average_point = (sum * 2) / 4;
//
//    //                    int amplitude = pow(((int)(average_point * 100)), 0.5);
//                        int amplitude = pow(((int)(((sum * 2) / 4) * 100)), 0.5);
//
//                        resultFrame.append(std::to_string(  amplitude ));
//                        resultFrame.append("\n");

                     } else {
                         __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "WAV\n");
                         __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "CHANNELS = %d\n", codecContext->channels);
                         __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "WAV\n", codecContext->);


                     }


                }
                else
                {
                    decodingPacket.size = 0;
                    decodingPacket.data = nullptr;
                }
            }
        }

        av_free_packet(&readingPacket);
    }

    av_free(frame);
    avcodec_close(codecContext);
    avformat_close_input(&formatContext);
    return env->NewStringUTF(resultFrame.data());
}

*/


/*


#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

std::string ConvertJString(JNIEnv* env, jstring str){
    if ( !str ) std::string();
    const jsize len = env->GetStringUTFLength(str);
    const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);
    std::string Result(strChars, len);
    env->ReleaseStringUTFChars(str, strChars);
    return Result;
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

extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
        ) {

    std::string filename = ConvertJString( env, audio_path );
    std::string resultFrame;

    av_register_all();

    AVFrame* frame = av_frame_alloc();

    if (!frame){
        return env->NewStringUTF("Error allocating the frame! Amplituda:exception:600");
    }

    AVFormatContext* formatContext = NULL;
    if (avformat_open_input(&formatContext, filename.data(), NULL, NULL) != 0) {
        av_free(frame);
        // FIXME: error with same file form waveformSeekBar
        return env->NewStringUTF("Error opening file, file path is not valid! Amplituda:exception:800");
    }

    if (avformat_find_stream_info(formatContext, NULL) < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Error finding the stream information! Amplituda:exception:57005");
    }

    AVCodec* cdc = nullptr;
    int streamIndex = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &cdc, 0);
    if (streamIndex < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Could not find any audio stream in the file! Amplituda:exception:165");
    }

    AVStream* audioStream = formatContext->streams[streamIndex];
    AVCodecContext* codecContext = audioStream->codec;
    codecContext->codec = cdc;

    if (avcodec_open2(codecContext, codecContext->codec, NULL) != 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Couldn't open the context with the decoder! (can't find or open context) Amplituda:exception:1057");
    }

    AVPacket readingPacket;
    av_init_packet(&readingPacket);

    while (av_read_frame(formatContext, &readingPacket) == 0)
    {
        if (readingPacket.stream_index == audioStream->index)
        {
            AVPacket decodingPacket = readingPacket;
            while (decodingPacket.size > 0)
            {
                int gotFrame = 0;
                int result = avcodec_decode_audio4(codecContext, frame, &gotFrame, &decodingPacket);

                if (result >= 0 && gotFrame)
                {
                    decodingPacket.size -= result;
                    decodingPacket.data += result;

                    float sum = 0;
                    for(int s = 0; s < 4; ++s) {
                        for(int c = 0; c < codecContext->channels; ++c) {
                            float sample = getSample(codecContext, frame->extended_data[c], s);
                            if(sample < 0)
                                sum += -sample;
                            else
                                sum += sample;
                        }
                    }
//                    float average_point = (sum * 2) / 4;

//                    int amplitude = pow(((int)(average_point * 100)), 0.5);
                    int amplitude = pow(((int)(((sum * 2) / 4) * 100)), 0.5);

                    resultFrame.append(std::to_string(  amplitude ));
                    resultFrame.append("\n");
                }
                else
                {
                    decodingPacket.size = 0;
                    decodingPacket.data = nullptr;
                }
            }
        }

        av_free_packet(&readingPacket);
    }

    av_free(frame);
    avcodec_close(codecContext);
    avformat_close_input(&formatContext);
    return env->NewStringUTF(resultFrame.data());
}


*/