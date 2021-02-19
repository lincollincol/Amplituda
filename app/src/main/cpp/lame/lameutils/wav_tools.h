//
// Created by 冉高飞 on 2018/5/3.
//

#ifndef AUDIOAPPLICATION_WAV_TOOLS_H
#define AUDIOAPPLICATION_WAV_TOOLS_H


#include <iostream>
#include <fstream>
#include "../saka_log.h"

#define STATE_SUCCESS 0
#define STATE_ERR_NOT_WAV 1

const char RIFF[] = "RIFF";
const char WAVE[] = "WAVE";
const char FMT[] = "fmt ";
const char DATA[] = "data";

struct wave_format_ {
    char fmt[4];
    uint32_t chunk_size;
    uint16_t audio_format;
    uint16_t num_channels;
    uint32_t sample_rate;
    uint32_t byte_rate;
    uint16_t block_align;
    uint16_t bit_per_sample;
};

struct wave_data_ {
    char data[4];
    uint32_t chunk_size;
};

typedef wave_format_ FmtChunk;
typedef wave_data_ DataChunk;

class wav_tools {
public:
    wav_tools();

    ~wav_tools();

    int getWavFormat(std::ifstream *in_stream, FmtChunk *fmtChunk);

    int seekToRealData(std::ifstream *in_stream, uint32_t *ptr_position);

    int getFileWavFormat(FILE *file, FmtChunk *fmtChunk);

    int seekToFileRealData(FILE *file,uint32_t *ptr_position);

private:
    char tmp_header[5];
    uint32_t chunk_size;

};


#endif //AUDIOAPPLICATION_WAV_TOOLS_H
