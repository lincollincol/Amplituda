//
// Created by 冉高飞 on 2018/5/3.
//

#include <cstring>
#include "wav_tools.h"

wav_tools::wav_tools() {
    memset(tmp_header, 0, 5 * sizeof(char));

}

wav_tools::~wav_tools() {
    free(tmp_header);

}

int wav_tools::getWavFormat(std::ifstream *in_stream, FmtChunk *fmtChunk) {
    if (!in_stream->is_open()) {
        std::cout << "the stream is null" << std::endl;
        return -1;
    }
    in_stream->seekg(0, std::ios::beg);
    in_stream->read(tmp_header, 4);
    if (strcmp(tmp_header, RIFF) != 0) {
        std::cout << "tmp_header:" << tmp_header << std::endl;
        return STATE_ERR_NOT_WAV;
    }
    in_stream->seekg(8, std::ios::cur);
    /*
    char fmt[4];
    uint32_t chunk_size;
    uint16_t audio_format;
    uint16_t num_channels;
    uint32_t sample_rate;
    uint32_t byte_rate;
    uint16_t block_align;
    uint16_t bit_per_sample;
     */
    while (true) {
        in_stream->read(tmp_header, 4);
        if (strcmp(tmp_header, FMT) != 0) {
            in_stream->read((char *) &chunk_size, sizeof(uint32_t));
            in_stream->seekg(chunk_size, std::ios::cur);
            continue;
        } else {
            in_stream->seekg(-4, std::ios::cur);
            in_stream->read((char *) fmtChunk, sizeof(FmtChunk));
            SAKA_LOG_DEBUG("The format chunk:\nchunk_name:%s\n"
                                   "chunk_size:%d"
                                   "audio_format:%d"
                                   "num_channels:%d"
                                   "sample_rate:%d"
                                   "byte_rate:%d"
                                   "block_align:%d"
                                   "bit_per_samle:%d",
                           fmtChunk->fmt, fmtChunk->chunk_size, fmtChunk->audio_format,
                           fmtChunk->num_channels,
                           fmtChunk->sample_rate, fmtChunk->byte_rate, fmtChunk->block_align,
                           fmtChunk->bit_per_sample);
            break;
        }

    }
    return STATE_SUCCESS;
}

int wav_tools::seekToRealData(std::ifstream *in_stream, uint32_t *ptr_position) {
    if (!(in_stream->is_open())) {
        std::cout << "the stream is null" << std::endl;
        return -1;
    }
    in_stream->seekg(0, std::ios::beg);
    char tmp_header[5];
    memset(tmp_header, 0, 5 * sizeof(char));
    in_stream->read(tmp_header, 4);
    if (strcmp(tmp_header, RIFF) != 0) {
        std::cout << "tmp_header:" << tmp_header << std::endl;
        return STATE_ERR_NOT_WAV;
    }
    in_stream->seekg(8, std::ios::cur);
    while (true) {
        in_stream->read(tmp_header, 4);
        if (strcmp(tmp_header, DATA) != 0) {
            in_stream->read((char *) &chunk_size, sizeof(uint32_t));
            in_stream->seekg(chunk_size, std::ios::cur);
            continue;
        } else {
            auto *dataChunk = static_cast<DataChunk *>(malloc(sizeof(DataChunk)));
            in_stream->seekg(-4, std::ios::cur);
            in_stream->read((char *) dataChunk, sizeof(DataChunk));
            SAKA_LOG_DEBUG("data_chunk:\nchunk_name:%s\n,chunk_size:%d", dataChunk->data,
                           dataChunk->chunk_size);
            break;
        }

    }
    *ptr_position = (uint32_t) in_stream->gcount();
    return STATE_SUCCESS;
}

int wav_tools::getFileWavFormat(FILE *file, FmtChunk *fmtChunk) {
    if (!file) {
        std::cout << "the file is null" << std::endl;
        return -1;
    }
    fseek(file, 0, SEEK_SET);
    fread(tmp_header, sizeof(char), 4, file);
    if (strcmp(tmp_header, RIFF) != 0) {
        std::cout << "tmp_header:" << tmp_header << std::endl;
        return STATE_ERR_NOT_WAV;
    }
    fseek(file, 8, SEEK_CUR);
    /*
    char fmt[4];
    uint32_t chunk_size;
    uint16_t audio_format;
    uint16_t num_channels;
    uint32_t sample_rate;
    uint32_t byte_rate;
    uint16_t block_align;
    uint16_t bit_per_sample;
     */
    while (true) {
        fread(tmp_header, sizeof(char), 4, file);
        if (strcmp(tmp_header, FMT) != 0) {
            fread(&chunk_size, sizeof(uint32_t), 1, file);
            fseek(file, chunk_size, SEEK_CUR);
            continue;
        } else {
            fseek(file, -4, SEEK_CUR);
            fread(fmtChunk, sizeof(FmtChunk), 1, file);
            SAKA_LOG_DEBUG("The format chunk:\nchunk_name:%s\n"
                                   "chunk_size:%d\n"
                                   "audio_format:%d\n"
                                   "num_channels:%d\n"
                                   "sample_rate:%d\n"
                                   "byte_rate:%d\n"
                                   "block_align:%d\n"
                                   "bit_per_samle:%d\n",
                           fmtChunk->fmt, fmtChunk->chunk_size, fmtChunk->audio_format,
                           fmtChunk->num_channels,
                           fmtChunk->sample_rate, fmtChunk->byte_rate, fmtChunk->block_align,
                           fmtChunk->bit_per_sample);
            break;
        }

    }
    return STATE_SUCCESS;
}

int wav_tools::seekToFileRealData(FILE *file, uint32_t *ptr_position) {
    if (!file) {
        std::cout << "the file is null" << std::endl;
        return -1;
    }
    fseek(file, 0, SEEK_SET);
    fread(tmp_header, sizeof(char), 4, file);
    if (strcmp(tmp_header, RIFF) != 0) {
        std::cout << "tmp_header:" << tmp_header << std::endl;
        return STATE_ERR_NOT_WAV;
    }
    fseek(file, 8, SEEK_CUR);
    while (true) {
        fread(tmp_header, sizeof(char), 4, file);
        if (strcmp(tmp_header, DATA) != 0) {
            fread(&chunk_size, sizeof(uint32_t), 1, file);
            fseek(file, chunk_size, SEEK_CUR);
            continue;
        } else {
            auto *dataChunk = static_cast<DataChunk *>(malloc(sizeof(DataChunk)));
            fseek(file, -4, SEEK_CUR);
            fread(dataChunk, sizeof(DataChunk), 1, file);
            SAKA_LOG_DEBUG("data_chunk:\nchunk_name:%s\n,chunk_size:%d", dataChunk->data,
                           dataChunk->chunk_size);
            break;
        }

    }
    *ptr_position = (uint32_t) ftell(file);
    return STATE_SUCCESS;
}
