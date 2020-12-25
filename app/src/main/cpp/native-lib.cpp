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


/*


#include <iostream>
#include <string>
#include <fstream>
#include <cmath>

using namespace std;
using std::string;
using std::fstream;

typedef struct  WAV_HEADER
{

uint8_t             RIFF[4];        // RIFF Header      Magic header
uint32_t            ChunkSize;      // RIFF Chunk Size
uint8_t             WAVE[4];        // WAVE Header

uint8_t             fmt[4];         // FMT header
uint32_t            Subchunk1Size;  // Size of the fmt chunk
uint16_t            AudioFormat;    // Audio format 1=PCM,6=mulaw,7=alaw, 257=IBM Mu-Law, 258=IBM A-Law, 259=ADPCM
uint16_t            NumOfChan;      // Number of channels 1=Mono 2=Sterio
uint32_t             SamplesPerSec;  // Sampling Frequency in Hz
uint32_t             bytesPerSec;    // bytes per second
uint16_t             blockAlign;     // 2=16-bit mono, 4=16-bit stereo
uint16_t             bitsPerSample;  // Number of bits per sample

uint8_t              Subchunk2ID[4]; // "data"  string
uint32_t             Subchunk2Size;  // Sampled data length

}wav_hdr;


int getFileSize(FILE* inFile);
float getSample(uint16_t bps, uint8_t* buffer, int sampleIndex);

int main(int argc, char* argv[])
{

    FILE *amps = fopen("/home/linc/Музика/amps_.txt", "a+");

    wav_hdr wavHeader;
    int headerSize = sizeof(wav_hdr), filelength = 0;

    const char* filePath;
    string input;
    if (argc <= 1)
    {
        cout << "Input wave file name: /home/linc/Музика/kygo.mp3";
//        cin >> input;
//        cin.get();
        filePath = "/home/linc/Музика/kygo.wav";
    }
    else
    {
        filePath = argv[1];
        cout << "Input wave file name: " << filePath << endl;
    }

    FILE* wavFile = fopen(filePath, "r");
    if (wavFile == nullptr)
    {
        fprintf(stderr, "Unable to open wave file: %s\n", filePath);
        return 1;
    }

//Read the header
    size_t bytesRead = fread(&wavHeader, 1, headerSize, wavFile);
    cout << "Header Read " << bytesRead << " bytes." << endl;
    if (bytesRead > 0)
    {
        //Read the data
        uint16_t bytesPerSample = wavHeader.bitsPerSample / 8;      //Number     of bytes per sample
        uint64_t numSamples = wavHeader.ChunkSize / bytesPerSample; //How many samples are in the wav file?
        static const uint16_t BUFFER_SIZE = 4096;
        uint8_t* buffer = new uint8_t[BUFFER_SIZE];

        int s_index = 0;

        while ((bytesRead = fread(buffer, sizeof buffer[0], BUFFER_SIZE / (sizeof buffer[0]), wavFile)) > 0)
        {

            float sample = getSample(bytesPerSample, buffer, s_index);

            if(sample < 0)
                sample = -sample;


            int amplitude = static_cast<int>(pow(((int)(((sample * 2) / 4) * 100)), 0.5));
            fprintf(amps, "%d\n", amplitude);

            cout << "Read " << amplitude << " bytes." << endl;

            s_index++;
        }
        delete [] buffer;
        buffer = nullptr;
        filelength = getFileSize(wavFile);

        cout << "File is :" << filelength << " bytes." << endl;
        cout << "RIFF header :" << wavHeader.RIFF[0] << wavHeader.RIFF[1] << wavHeader.RIFF[2] << wavHeader.RIFF[3] << endl;
        cout << "WAVE header :" << wavHeader.WAVE[0] << wavHeader.WAVE[1] << wavHeader.WAVE[2] << wavHeader.WAVE[3] << endl;
        cout << "FMT :" << wavHeader.fmt[0] << wavHeader.fmt[1] << wavHeader.fmt[2] << wavHeader.fmt[3] << endl;
        cout << "Data size :" << wavHeader.ChunkSize << endl;

        // Display the sampling Rate from the header
        cout << "Sampling Rate :" << wavHeader.SamplesPerSec << endl;
        cout << "Number of bits used :" << wavHeader.bitsPerSample << endl;
        cout << "Number of channels :" << wavHeader.NumOfChan << endl;
        cout << "Number of bytes per second :" << wavHeader.bytesPerSec << endl;
        cout << "Data length :" << wavHeader.Subchunk2Size << endl;
        cout << "Audio Format :" << wavHeader.AudioFormat << endl;
        // Audio format 1=PCM,6=mulaw,7=alaw, 257=IBM Mu-Law, 258=IBM A-Law, 259=ADPCM

        cout << "Block align :" << wavHeader.blockAlign << endl;
        cout << "Data string :" << wavHeader.Subchunk2ID[0] << wavHeader.Subchunk2ID[1] << wavHeader.Subchunk2ID[2] << wavHeader.Subchunk2ID[3] << endl;


    }
    fclose(wavFile);
    fclose(amps);

    return 0;
}

// find the file size
int getFileSize(FILE* inFile)
{
    int fileSize = 0;
    fseek(inFile, 0, SEEK_END);

    fileSize = ftell(inFile);

    fseek(inFile, 0, SEEK_SET);
    return fileSize;
}

float getSample(uint16_t bps, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    float ret = 0;
    int sampleSize = bps;

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

    ret = val / (static_cast<float>(((1 << (sampleSize*8-1))-1)));

    return ret;

}

 */