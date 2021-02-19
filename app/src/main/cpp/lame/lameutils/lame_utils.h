//
// Created by 冉高飞 on 2018/5/2.
//

#ifndef AUDIOAPPLICATION_LAME_UTILS_H
#define AUDIOAPPLICATION_LAME_UTILS_H

#define LAME_ERR_SUCCESS 0;//success

#define LAME_ERR_INIT_FAILED 1          //lame init failed,may not enough mem

#define LAME_ERR_WRONG_CHANNEL 2        //lame's channel num wrong

#define LAME_ERR_WRONG_BUFFER_SIZE 3    //shuoud be odd, but is even

//#include <lame.h>
#include <stdio.h>
#include <stdlib.h>
#include "../include/lame.h"

struct LamePara_s {
    int num_channels;
    int sample_rate;
    int brate;
    MPEG_mode mode;
    int quality;
};
typedef LamePara_s LamePara;

class lame_utils {
public:
    lame_utils();

    ~lame_utils();

    int lame_encode(char *buffer, const int buffer_size,
                    unsigned char *mp3_buffer, const int mp3_size);

    int init_param(LamePara *lamePara);

    void lame_destroy();

    int get_mp3_buffer_size(int *mp3buffer_size, int sample_nums);


private:
    lame_t lame_client;
    short int *left_buffer;
    short int *right_buffer;
    uint32_t error_code;
};


#endif //AUDIOAPPLICATION_LAME_UTILS_H
