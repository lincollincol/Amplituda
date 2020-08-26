#include <jni.h>
#include <string>

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

Java_linc_com_amplituda_Amplituda_stringFromJNI(
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
        return env->NewStringUTF("Error opening file! Amplituda:exception:800");
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