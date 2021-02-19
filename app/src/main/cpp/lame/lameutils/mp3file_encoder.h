//
// Created by 冉高飞 on 2018/5/3.
//

#ifndef AUDIOAPPLICATION_MP3FILE_ENCODER_H
#define AUDIOAPPLICATION_MP3FILE_ENCODER_H


#include <jni.h>
#include "wav_tools.h"
#include "../include/lame.h"

class mp3file_encoder {
private:
    FILE *pcmFile;
    FILE *mp3File;
    lame_t lameClient;
    wav_tools wavTools;

public:
    mp3file_encoder();

    ~mp3file_encoder();

    int Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate, int channels,
             int bitrate);

    void Encode();

    void Destroy();

};


#endif //AUDIOAPPLICATION_MP3FILE_ENCODER_H
