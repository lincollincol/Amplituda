//
// Created by linc on 18.12.20.
//

#ifndef PCMDECODER_WAVFILE_ENCODER_H
#define PCMDECODER_WAVFILE_ENCODER_H


#include "stdio.h"
#include "wav_tools.h"
#include "../include/lame.h"

class wavfile_encoder {
private:
    FILE *pcmFile;
    FILE *mp3File;
    lame_t lameClient;
    wav_tools wavTools;

public:
    wavfile_encoder();

    ~wavfile_encoder();

    int Init(const char *pcmFilePath, const char *mp3FilePath, int sampleRate, int channels,
             int bitrate);

    void Encode();

    void Destroy();
};


#endif //PCMDECODER_WAVFILE_ENCODER_H
