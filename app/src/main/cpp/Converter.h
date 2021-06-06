//
// Created by linc on 06.06.21.
//

#ifndef AMPLITUDA_CONVERTER_H
#define AMPLITUDA_CONVERTER_H

#include <jni.h>
#include <android/log.h>
#include "lame/include/lame.h"
#define SAMPLE_RATE 44100

#ifdef __cplusplus
extern "C" {
#endif

int wav2mp3(const char* input, const char* output);

#ifdef __cplusplus
}
#endif


#endif //AMPLITUDA_CONVERTER_H
