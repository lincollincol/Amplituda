//
// Created by 冉高飞 on 2018/5/2.
//

#include <cstring>
#include "lame_utils.h"


lame_utils::lame_utils() {

}

lame_utils::~lame_utils() {
//    lame_destroy();

}

void lame_utils::lame_destroy() {
    if (lame_client) {
        lame_close(lame_client);
        lame_client = nullptr;
    }
    delete[] left_buffer;
    delete[] right_buffer;
    left_buffer = nullptr;
    right_buffer = nullptr;

}

int lame_utils::init_param(LamePara *lamePara) {
    lame_client = lame_init();
    if (lame_client == nullptr) {
        return LAME_ERR_INIT_FAILED;
    }
    lame_set_num_channels(lame_client, lamePara->num_channels);
    lame_set_in_samplerate(lame_client, lamePara->sample_rate);
    lame_set_brate(lame_client, lamePara->brate);
    lame_set_mode(lame_client, lamePara->mode);
    lame_set_quality(lame_client, lamePara->quality);
    return lame_init_params(lame_client);
}

int lame_utils::lame_encode(char *buffer, const int buffer_size,
                            unsigned char *mp3_buffer, const int mp3_size) {
    int write_size = -1;
    if (lame_get_num_channels(lame_client) == 1) {
        if (buffer_size % 2 != 0) {
            return LAME_ERR_WRONG_BUFFER_SIZE;
        }
        memcpy(left_buffer, buffer, buffer_size);
        memcpy(right_buffer, buffer, buffer_size);
        write_size = lame_encode_buffer(lame_client,
                                        left_buffer,
                                        right_buffer,
                                        buffer_size/2,
                                        mp3_buffer,
                                        mp3_size);
    } else if (lame_get_num_channels(lame_client) == 2) {
        write_size = lame_encode_buffer_interleaved(lame_client, (short *) buffer, buffer_size / 2,
                                                    mp3_buffer,
                                                    buffer_size / 2);

    } else {
        return LAME_ERR_WRONG_CHANNEL;
    }
    return write_size;
}

int lame_utils::get_mp3_buffer_size(int *mp3buffer_size, int sample_nums) {

    *mp3buffer_size = (int) (1.25 * sample_nums + 7200);
    left_buffer = new short int[sample_nums];
    right_buffer = new short int[sample_nums];
    return 0;
}

