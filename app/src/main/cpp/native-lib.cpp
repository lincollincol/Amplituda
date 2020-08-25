#include <jni.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
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

        case 2:
            val = (reinterpret_cast<uint16_t*>(buffer))[sampleIndex];
            break;

        case 4:
            val = (reinterpret_cast<uint32_t*>(buffer))[sampleIndex];
            break;

        case 8:
            val = (reinterpret_cast<uint64_t*>(buffer))[sampleIndex];
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
//            fprintf(stderr, "Invalid sample format %s.\n", av_get_sample_fmt_name(codecCtx->sample_fmt));
            return 0;
    }

    return ret;

}

extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_stringFromJNI(
        JNIEnv* env,
        jobject) {
    // You can change the filename for any other filename/supported format
//    const char *filename = "../my file.ogg";

//    std::string resultFrame = "";

    // SUPPORTED AUDIO FORMATS
    // [mp3, opus, oga, ogg, m4a, mp4] / 1h audio processing = 30sec

//    const char *filename = "/storage/emulated/0/viber/kygo.mp3";
//    const char *filename = "/storage/emulated/0/viber/ex.wav";
    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/music/f_voip_dur.opus";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/music/Голос 0017325996153317080688.m4a";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/video_notes/5406735884265457666.mp4";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/voice/5382102159468791418.oga";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/music/DiscDj_Rec_2020-06-21_18-38-44.mp3";

    // Initialize FFmpeg
    av_register_all();

//    AVFrame* frame = avcodec_alloc_frame();
    AVFrame* frame = av_frame_alloc();

    if (!frame)
    {
//        std::cout << "Error allocating the frame.  Let's try again shall we?\n";
        return env->NewStringUTF("666");  // fail at start: 66 = number of the beast
    }

    // you can change the file name to whatever yo need:)
    AVFormatContext* formatContext = NULL;
    if (avformat_open_input(&formatContext, filename, NULL, NULL) != 0)
    {
        av_free(frame);
//        std::cout << "Error opening file " << filename<< "\n";
        return env->NewStringUTF("800"); // cant open file.  800 = Boo!
    }

    if (avformat_find_stream_info(formatContext, NULL) < 0)
    {
        av_free(frame);
        avformat_close_input(&formatContext);
//        std::cout << "Error finding the stream information.\nCheck your paths/connections and the details you supplied!\n";
        return env->NewStringUTF("57005"); // stream info error.  0xDEAD in hex is 57005 in decimal
    }

    // Find the audio stream
    AVCodec* cdc = nullptr;
    int streamIndex = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &cdc, 0);
    if (streamIndex < 0)
    {
        av_free(frame);
        avformat_close_input(&formatContext);
//        std::cout << "Could not find any audio stream in the file.  Come on! I need data!\n";
        return env->NewStringUTF("165"); // no(0) (a)udio s(5)tream:  0A5 in hex = 165 in decimal
    }

    AVStream* audioStream = formatContext->streams[streamIndex];
    AVCodecContext* codecContext = audioStream->codec;
    codecContext->codec = cdc;

    if (avcodec_open2(codecContext, codecContext->codec, NULL) != 0)
    {
        av_free(frame);
        avformat_close_input(&formatContext);
//        std::cout << "Couldn't open the context with the decoder.  I can decode but I need to have something to decode.\nAs I couldn't find anything I have surmised the decoded output is 0!\n (Well can't have you thinking I am doing nothing can we?\n";
        return env->NewStringUTF("1057"); // cant find/open context 1057 = lost
    }

//    std::cout << "This stream has " << codecContext->channels << " channels with a sample rate of " << codecContext->sample_rate << "Hz\n";
//    std::cout << "The data presented in format: " << av_get_sample_fmt_name(codecContext->sample_fmt) << std::endl;

    AVPacket readingPacket;
    av_init_packet(&readingPacket);

    // Read the packets in a loop
    while (av_read_frame(formatContext, &readingPacket) == 0)
    {
        if (readingPacket.stream_index == audioStream->index)
        {
            AVPacket decodingPacket = readingPacket;
            // Audio packets can have multiple audio frames in a single packet
            while (decodingPacket.size > 0)
            {
                // Try to decode the packet into a frame(s)
                // Some frames rely on multiple packets, so we have to make sure the frame is finished
                // before utilising it
                int gotFrame = 0;
                int result = avcodec_decode_audio4(codecContext, frame, &gotFrame, &decodingPacket);

                if (result >= 0 && gotFrame)
                {
                    decodingPacket.size -= result;
                    decodingPacket.data += result;

                    float sum = 0;
//                    for(int s = 0; s < frame->nb_samples; ++s) {
                    for(int s = 0; s < 4; ++s) {
                        for(int c = 0; c < codecContext->channels; ++c) {
                            float sample = getSample(codecContext, frame->extended_data[c], s);
                            if(sample < 0)
                                sum += -sample;
                            else
                                sum += sample;
//                            __android_log_print(ANDROID_LOG_VERBOSE, "NDK_NDK_NDK", "First loop = %f", sample);
                        }
                    }
                    float average_point = (sum * 2) / 4;

                    int ampl_av = pow(((int)(average_point * 100)), 0.5);

//                    resultFrame.append(std::to_string(  ampl_av ));
//                    resultFrame.append("\n");
                }
                else
                {
                    decodingPacket.size = 0;
                    decodingPacket.data = nullptr;
                }
            }
        }

        // You MUST call av_free_packet() after each call to av_read_frame()
        // or you will leak so much memory on a large file you will need a memory-plumber!
        av_free_packet(&readingPacket);
    }

    // Clean up! (unless you have a quantum memory machine with infinite RAM....)
    av_free(frame);
    avcodec_close(codecContext);
    avformat_close_input(&formatContext);
//    return env->NewStringUTF(resultFrame.data()); // success!!!!!!!!
    return env->NewStringUTF("0"); // success!!!!!!!!
}