//
// Created by linc on 18.12.20.
//

#include "wavfile_encoder.h"
#include "../saka_log.h"



wavfile_encoder::wavfile_encoder() {
    const char *version = get_lame_version();
    SAKA_LOG_DEBUG("%s", version);
}

wavfile_encoder::~wavfile_encoder() {

}

int wavfile_encoder::Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate,
                          int channels, int bitrate) {
    /*int ret = -1;
    pcmFile = fopen(pcmFilePath, "rb");
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
            lame_set_brate(lameClient, bitrate / 1000);
            lame_init_params(lameClient);
            ret = 0;
            SAKA_LOG_DEBUG("Init lame success");
        }
    }*/
    return -1;

}

void wavfile_encoder::Encode() {
    try {
        int read, write;
        FILE *pcm = fopen("/storage/emulated/0/Music/kygo.wav", "rb");  //source
        fseek(pcm, 4*1024, SEEK_CUR);                                   //skip file header

        FILE *mp3 = fopen("/storage/emulated/0/Music/kg.mp3", "wb");  //output
        const int PCM_SIZE = 8192*3;
        const int MP3_SIZE = 8192*3;
        short int pcm_buffer[PCM_SIZE*2];
        unsigned char mp3_buffer[MP3_SIZE];

        lame_t lame = lame_init();
        lame_set_in_samplerate(lame, 11025*2);
        lame_set_VBR(lame, vbr_default);
        lame_init_params(lame);

        int nTotalRead=0;

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

        lame_close(lame);
        fclose(mp3);
        fclose(pcm);

    } catch (...) {
        SAKA_LOG_DEBUG("---------------exception in the wav scope---------------");

        //NSLog(@"%@",[exception description]);
    }

}

void wavfile_encoder::Destroy() {
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
