//
// Created by linc on 06.06.21.
//

#include "Converter.h"

int wav2mp3(const char* input, const char* output) {
    int read, write;

    // Open input wav
    FILE *pcm = fopen(input, "rb");

    // Skip wav header
    fseek(pcm, 4 * 1024, SEEK_CUR);

    // Open output temp mp3 file
    FILE *mp3 = fopen(output, "wb");

    const int PCM_SIZE = 8192 * 3;
    const int MP3_SIZE = 8192 * 3;

    short int pcm_buffer[PCM_SIZE * 2];
    unsigned char mp3_buffer[MP3_SIZE];

    // Prepare lame
    lame_t lame = lame_init();
    lame_set_in_samplerate(lame, SAMPLE_RATE);
    lame_set_VBR(lame, vbr_default);
    lame_init_params(lame);

    int totalRead = 0;

    // Convert wav to mp3
    do {
        read = fread(pcm_buffer, 2 * sizeof(short int), PCM_SIZE, pcm);
        totalRead += read * 4;

        if (read == 0) write = lame_encode_flush(lame, mp3_buffer, MP3_SIZE);
        else write = lame_encode_buffer_interleaved(lame, pcm_buffer, read, mp3_buffer, MP3_SIZE);

        fwrite(mp3_buffer, write, 1, mp3);
    } while (read != 0);

    // clean up lame
    lame_close(lame);
    fclose(mp3);
    fclose(pcm);

    return 0;
}
