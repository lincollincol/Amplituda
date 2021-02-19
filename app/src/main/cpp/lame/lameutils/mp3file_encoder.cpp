//
// Created by 冉高飞 on 2018/5/3.
//

#include "mp3file_encoder.h"
#include "../saka_log.h"

mp3file_encoder::mp3file_encoder() {
    const char *version = get_lame_version();
    SAKA_LOG_DEBUG("%s", version);

}

mp3file_encoder::~mp3file_encoder() {

}

int mp3file_encoder::Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate,
                          int channels, int bitrate) {

    int ret = -1;
    /*pcmFile = fopen(pcmFilePath, "rb");
    if (pcmFile) {
        mp3File = fopen(mp3FilePath, "wb");
        if (mp3File) {
            uint32_t ptr = 0;
            FmtChunk fmtChunk;
            wavTools.getFileWavFormat(pcmFile, &fmtChunk);
            wavTools.seekToFileRealData(pcmFile, &ptr);
            lameClient = lame_init();



            lame_set_in_samplerate(lameClient, sampleRate);
            lame_set_out_samplerate(lameClient, sampleRate);
            lame_set_num_channels(lameClient, channels);

            if(channels == 1) {
                lame_set_mode(lameClient, MONO);
            }

            lame_set_brate(lameClient, bitrate / 1000);

//            lame_set_in_samplerate(lameClient, sampleRate);
//            lame_set_out_samplerate(lameClient, sampleRate);
//            lame_set_num_channels(lameClient, channels);
//            lame_set_brate(lameClient, bitrate / 1000);




            lame_init_params(lameClient);
            ret = 0;
            SAKA_LOG_DEBUG("Init lame success");
        }
    }*/
    return ret;

}

void decode_mp3() {
    /*FILE *fp = fopen("/storage/emulated/0/Music/amps.txt","a+");

    size_t bufferSize = 1024 * 256;
    short *buffer = new short[bufferSize / 2];
    short *leftBuffer = new short[bufferSize / 4];
    short *rightBuffer = new short[bufferSize / 4];
    unsigned char *mp3_buffer = new unsigned char[bufferSize];
    size_t readBufferSize = 0;
    while ((readBufferSize = fread(buffer, 2, bufferSize / 2, "pcmFile")) > 0) {
        for (int i = 0; i < readBufferSize; ++i) {
            if (i % 2 == 0) {
                leftBuffer[i / 2] = buffer[i];
            } else {
                rightBuffer[i / 2] = buffer[i];
            }
        }
        size_t wroteSize;

        if (lame_get_num_channels(lameClient) == 1) {
            wroteSize = lame_encode_buffer(
                    lameClient,
                    (short int *) leftBuffer,
                    NULL,
                    (int) (readBufferSize / 2),
                    mp3_buffer,
                    bufferSize
            );
        }else{
            wroteSize = lame_encode_buffer_interleaved(
                    lameClient,
                    (short int *) leftBuffer,
                    (int) (readBufferSize / 2),
                    mp3_buffer,
                    bufferSize
            );
        }



//        char amp[5];
        int max = 0;
        for(int i = 0; i < wroteSize; i++) {
            if(max > mp3_buffer[i]) {
                max = mp3_buffer[i];
            }

//            sprintf(amp, "%d\n", mp3_buffer[i]);
//            fputs(amp,fp);
        }

        SAKA_LOG_DEBUG("%d", max);


        fwrite(mp3_buffer, 1, wroteSize, mp3File);
    }
    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;

    fclose(fp);*/
}


void mp3file_encoder::Encode() {

    SAKA_LOG_DEBUG("START");

    int read, write;
    FILE *pcm = fopen("/storage/emulated/0/Music/igor.wav", "rb");  //source
    fseek(pcm, 4*1024, SEEK_CUR);                                   //skip file header

    FILE *mp3 = fopen("/storage/emulated/0/Music/igorok_lame.mp3", "wb");  //output
    const int PCM_SIZE = 8192*3;
    const int MP3_SIZE = 8192*3;
    short int pcm_buffer[PCM_SIZE*2];
    unsigned char mp3_buffer[MP3_SIZE];

    SAKA_LOG_DEBUG("SIZES");


    lame_t lame = lame_init();
//    lame_set_in_samplerate(lame, 11025*2);
    lame_set_in_samplerate(lame, 22050*2);
//    lame_set_in_samplerate(lame, 11025*2);
    lame_set_VBR(lame, vbr_default);
    lame_init_params(lame);

    int nTotalRead=0;

    SAKA_LOG_DEBUG("DO");


    do {
        read = fread(pcm_buffer, 2*sizeof(short int), PCM_SIZE, pcm);

        nTotalRead+=read*4;

        if (read == 0)
            write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
        else
            write = lame_encode_buffer_interleaved(lame,pcm_buffer, read, mp3_buffer, MP3_SIZE);
        // write = lame_encode_buffer(lame, pcm_buffer,pcm_buffer, read, mp3_buffer, MP3_SIZE);

        fwrite(mp3_buffer, write, 1, mp3);
    } while (read != 0);

    SAKA_LOG_DEBUG("END LOOP");

    lame_close(lame);
    fclose(mp3);
    fclose(pcm);

    SAKA_LOG_DEBUG("CLOSE");




    /*FILE *fp = fopen("/storage/emulated/0/Music/amps.txt","a+");

    size_t bufferSize = 1024 * 256;
    short *buffer = new short[bufferSize / 2];
    short *leftBuffer = new short[bufferSize / 4];
    short *rightBuffer = new short[bufferSize / 4];
    unsigned char *mp3_buffer = new unsigned char[bufferSize];
    size_t readBufferSize = 0;
    while ((readBufferSize = fread(buffer, 2, bufferSize / 2, pcmFile)) > 0) {
        for (int i = 0; i < readBufferSize; ++i) {
            if (i % 2 == 0) {
                leftBuffer[i / 2] = buffer[i];
            } else {
                rightBuffer[i / 2] = buffer[i];
            }
        }
        size_t wroteSize;

        if (lame_get_num_channels(lameClient) == 1) {
            wroteSize = lame_encode_buffer(
                    lameClient,
                    (short int *) leftBuffer,
                    NULL,
                    (int) (readBufferSize / 2),
                    mp3_buffer,
                    bufferSize
            );
        }else{
            wroteSize = lame_encode_buffer_interleaved(
                    lameClient,
                    (short int *) leftBuffer,
                    (int) (readBufferSize / 2),
                    mp3_buffer,
                    bufferSize
            );
        }



//        char amp[5];
        int max = 0;
        for(int i = 0; i < wroteSize; i++) {
            if(max > mp3_buffer[i]) {
                max = mp3_buffer[i];
            }

//            sprintf(amp, "%d\n", mp3_buffer[i]);
//            fputs(amp,fp);
        }

        SAKA_LOG_DEBUG("%d", max);


        fwrite(mp3_buffer, 1, wroteSize, mp3File);
    }
    delete[] buffer;
    delete[] leftBuffer;
    delete[] rightBuffer;
    delete[] mp3_buffer;

    fclose(fp);*/
}

void mp3file_encoder::Destroy() {
    if (lameClient) {
        lame_close(lameClient);
    }
    if (pcmFile) {
        fclose(pcmFile);
    }
    if (mp3File) {
        fclose(mp3File);
    }

}
