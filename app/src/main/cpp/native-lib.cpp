#include <jni.h>
#include <string>
#include <android/log.h>


extern "C" {

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"

}

std::string ConvertJString(JNIEnv* env, jstring str){
    if ( !str ) std::string();
    const jsize len = env->GetStringUTFLength(str);
    const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);
    std::string Result(strChars, len);
    env->ReleaseStringUTFChars(str, strChars);
    return Result;
}


// normalized version of the AVSampleFormat enum that doesn't care about planar vs interleaved
enum SampleFormat {
    SAMPLE_FORMAT_UINT8,
    SAMPLE_FORMAT_INT16,
    SAMPLE_FORMAT_INT32,
    SAMPLE_FORMAT_FLOAT,
    SAMPLE_FORMAT_DOUBLE
};

// struct to store the raw important data of an audio file pulled from ffmpeg
typedef struct AudioData {
    /*
     * The `samples` buffer is an interleaved buffer of all the raw samples from the audio file.
     * This is populated by calling `read_audio_data`
     *
     * Recall that audio data can be either planar (one buffer or "plane" for each channel) or
     * interleaved (one buffer for all channels: in a stereo file, the first sample for the left
     * channel is at index 0 with the first sample for the right channel at index 1, the second
     * sample for the left channel is at index 2 with the second sample for the right channel at
     * index 3, etc.).
     *
     * To make things easier, data read from ffmpeg is normalized to an interleaved buffer and
     * pointed to by `samples`.
     */
    uint8_t *samples;

    /*
     * The size of the `samples` buffer. Not known until after a call to `read_audio_data` or
     * `read_audio_metadata`
     */
    int size;

    /*
     * Length of audio file in seconds. Not known until after a call to `read_audio_data` or
     * `read_audio_metadata`
     *
     * This is calculated after reading all the raw samples from the audio file,
     * making it much more accurate than what the header or a bit rate based
     * guess
     */
    double duration;

    /*
     * sample rate of the audio file (44100, 48000, etc). Not known until after a call
     * to `read_audio_data` or `read_audio_metadata`
     */
    int sample_rate;

    /*
     * Number of bytes per sample. Use together with `size` and `format` to pull data from
     * the `samples` buffer
     */
    int sample_size;

    /*
     * Tells us the number format of the audio file
     */
    enum SampleFormat format;

    // how many channels does the audio file have? 1 (mono)? 2 (stereo)? ...
    int channels;

    /*
     * Format context from ffmpeg, which is the wrapper for the input audio file
     */
    AVFormatContext *format_context;

    /*
     * Codec context from ffmpeg. This is what gets us to all the raw
     * audio data.
     */
    AVCodecContext *decoder_context;
} AudioData;




// close and free ffmpeg structs
void cleanup(AVFormatContext *pFormatContext, AVCodecContext *pDecoderContext) {
    avformat_close_input(&pFormatContext);
    avcodec_close(pDecoderContext);
}


// free memory allocated by an AudioData struct
void free_audio_data(AudioData *data) {
    cleanup(data->format_context, data->decoder_context);

    if (data->samples != NULL) {
        free(data->samples);
    }

    free(data);
}




float getSample(const AVCodecContext* codecCtx, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    float ret = 0;
    int sampleSize = av_get_bytes_per_sample(codecCtx->sample_fmt);

    switch(sampleSize) {
        case 1:
            val = (reinterpret_cast<uint8_t*>(buffer))[sampleIndex];
            val -= 127;
            break;
        case 2: val = (reinterpret_cast<uint16_t*>(buffer))[sampleIndex];
            break;
        case 4: val = (reinterpret_cast<uint32_t*>(buffer))[sampleIndex];
            break;
        case 8: val = (reinterpret_cast<uint64_t*>(buffer))[sampleIndex];
            break;
        default:
            return 0;
    }

    // Check which data type is in the sample.
    switch(codecCtx->sample_fmt) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_U8P:
        case AV_SAMPLE_FMT_S16P:
        case AV_SAMPLE_FMT_S32P:
            // integer => Scale to [-1, 1] and convert to float.
            ret = val / (static_cast<float>(((1 << (sampleSize*8-1))-1)));
            break;
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            // float => reinterpret
            ret = *reinterpret_cast<float*>(&val);
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
            // double => reinterpret and then static cast down
            ret = static_cast<float>(*reinterpret_cast<double*>(&val));
            break;
        default:
            return 0;
    }

    return ret;

}

// get the sample at the given index out of the audio file data.
//
// NOTE: This function expects the caller to know what index to grab based on
// the data's sample size and channel count. It does not magic of its own.
double get_sample(AudioData *data, int index) {
    double value = 0.0;

    switch (data->format) {
        case SAMPLE_FORMAT_UINT8:
            value += data->samples[index];
            break;
        case SAMPLE_FORMAT_INT16:
            value += ((int16_t *) data->samples)[index];
            break;
        case SAMPLE_FORMAT_INT32:
            value += ((int32_t *) data->samples)[index];
            break;
        case SAMPLE_FORMAT_FLOAT:
            value += ((float *) data->samples)[index];
            break;
        case SAMPLE_FORMAT_DOUBLE:
            value += ((double *) data->samples)[index];
            break;
    }

    // if the value is over or under the floating point range (which it perfectly fine
    // according to ffmpeg), we need to truncate it to still be within our range of
    // -1.0 to 1.0, otherwise some of our latter math will have a bad case of
    // the segfault sads.
    if (data->format == SAMPLE_FORMAT_DOUBLE || data->format == SAMPLE_FORMAT_FLOAT) {
        if (value < -1.0) {
            value = -1.0;
        } else if (value > 1.0) {
            value = 1.0;
        }
    }

    return value;
}



// get the min and max values a sample can have given the format and put them
// into the min and max out parameters
void get_format_range(enum SampleFormat format, int *min, int *max) {
    int size;

    // figure out the range of sample values we're dealing with
    switch (format) {
        case SAMPLE_FORMAT_FLOAT:
        case SAMPLE_FORMAT_DOUBLE:
            // floats and doubles have a range of -1.0 to 1.0
            // NOTE: It is entirely possible for a sample to go beyond this range. Any value outside
            // is considered beyond full volume. Be aware of this when doing math with sample values.
            *min = -1;
            *max = 1;

            break;
        case SAMPLE_FORMAT_UINT8:
            *min = 0;
            *max = 255;

            break;
        default:
            // we're dealing with integers, so the range of samples is going to be the min/max values
            // of signed integers of either 16 or 32 bit (24 bit formats get converted to 32 bit at
            // the AVFrame level):
            //  -32,768/32,767, or -2,147,483,648/2,147,483,647
            size = format == SAMPLE_FORMAT_INT16 ? 2 : 4;
            *min = pow(2, size * 8) / -2;
            *max = pow(2, size * 8) / 2 - 1;
    }
}

/*
 * Take an ffmpeg AVFormatContext and AVCodecContext struct and create and AudioData struct
 * that we can easily work with
 */
AudioData *create_audio_data_struct(AVFormatContext *pFormatContext, AVCodecContext *pDecoderContext) {
    // Make the AudioData object we'll be returning
    AudioData *data = (AudioData*)malloc(sizeof(AudioData));
    data->format_context = pFormatContext;
    data->decoder_context = pDecoderContext;
    switch (pDecoderContext->sample_fmt) {
        case AV_SAMPLE_FMT_U8: data->format = SAMPLE_FORMAT_UINT8;
            break;
        case AV_SAMPLE_FMT_S16: data->format = SAMPLE_FORMAT_INT16;
            break;
        case AV_SAMPLE_FMT_S32: data->format = SAMPLE_FORMAT_INT32;
            break;
        case AV_SAMPLE_FMT_FLT: data->format = SAMPLE_FORMAT_FLOAT;
            break;
        case AV_SAMPLE_FMT_DBL: data->format = SAMPLE_FORMAT_DOUBLE;
            break;
    }
    data->sample_size = (int) av_get_bytes_per_sample(pDecoderContext->sample_fmt); // *byte* depth
    data->channels = pDecoderContext->channels;
    data->samples = NULL;

    // normalize the sample format to an enum that's less verbose than AVSampleFormat.
    // We won't care about planar/interleaved
    switch (pDecoderContext->sample_fmt) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_U8P:
            data->format = SAMPLE_FORMAT_UINT8;
            break;
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S16P:
            data->format = SAMPLE_FORMAT_INT16;
            break;
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_S32P:
            data->format = SAMPLE_FORMAT_INT32;
            break;
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            data->format = SAMPLE_FORMAT_FLOAT;
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
            data->format = SAMPLE_FORMAT_DOUBLE;
            break;
        default:
            fprintf(stderr, "Bad format: %s\n", av_get_sample_fmt_name(pDecoderContext->sample_fmt));
            free_audio_data(data);
            return NULL;
    }

    return data;
}



/*
 * Iterate through the audio file, converting all compressed samples into raw samples.
 * This will populate all of the fields on the data struct, with the exception of
 * the `samples` buffer if `populate_sample_buffer` is set to 0
 */
static void read_raw_audio_data(AudioData *data, int populate_sample_buffer) {
    // Packets will contain chucks of compressed audio data read from the audio file.
    AVPacket packet;

    // Frames will contain the raw uncompressed audio data read from a packet
    AVFrame *pFrame = NULL;

    // how long in seconds is the audio file?
    double duration = data->format_context->duration / (double) AV_TIME_BASE;
    int raw_sample_rate = 0;

    // is the audio interleaved or planar?
    int is_planar = av_sample_fmt_is_planar(data->decoder_context->sample_fmt);

    // running total of how much data has been converted to raw and copied into the AudioData
    // `samples` buffer. This will eventually be `data->size`
    int total_size = 0;

    av_init_packet(&packet);

    if (!(pFrame = av_frame_alloc())) {
        fprintf(stderr, "Could not allocate AVFrame\n");
        free_audio_data(data);
        return;
    }

    int allocated_buffer_size = 0;

    // guess how much memory we'll need for samples.
    if (populate_sample_buffer) {
        allocated_buffer_size = (data->format_context->bit_rate / 8) * duration;
        data->samples = (uint8_t*)malloc(sizeof(uint8_t) * allocated_buffer_size);
    }

    // Loop through the entire audio file by reading a compressed packet of the stream
    // into the uncomrpressed frame struct and copy it into a buffer.
    // It's important to remember that in this context, even though the actual format might
    // be 16 bit or 24 bit or float with x number of channels, while we're copying things,
    // we are only dealing with an array of 8 bit integers.
    //
    // It's up to anything using the AudioData struct to know how to properly read the data
    // inside `samples`
    int frames = 0;
    while (av_read_frame(data->format_context, &packet) == 0) {
        frames++;
        // some audio formats might not contain an entire raw frame in a single compressed packet.
        // If this is the case, then decode_audio4 will tell us that it didn't get all of the
        // raw frame via this out argument.
        int frame_finished = 0;

        // Use the decoder to populate the raw frame with data from the compressed packet.
        if (avcodec_decode_audio4(data->decoder_context, pFrame, &frame_finished, &packet) < 0) {
            // unable to decode this packet. continue on to the next packet
            continue;
        }

        // did we get an entire raw frame from the packet?
        if (frame_finished) {
            // Find the size of all pFrame->extended_data in bytes. Remember, this will be:
            // data_size = pFrame->nb_samples * pFrame->channels * bytes_per_sample
            int data_size = av_samples_get_buffer_size(
                    is_planar ? &pFrame->linesize[0] : NULL,
                    data->channels,
                    pFrame->nb_samples,
                    data->decoder_context->sample_fmt,
                    1
            );

            if (raw_sample_rate == 0) {
                raw_sample_rate = pFrame->sample_rate;
            }

            // if we don't have enough space in our copy buffer, expand it
            if (populate_sample_buffer && total_size + data_size > allocated_buffer_size) {
                allocated_buffer_size = allocated_buffer_size * 1.25;
                data->samples = (uint8_t*)realloc(data->samples, allocated_buffer_size);
            }

            if (is_planar) {
                // normalize all planes into the interleaved sample buffer
                int i = 0;
                int c = 0;

                // data_size is total data overall for all planes.
                // iterate through extended_data and copy each sample into `samples` while
                // interleaving each channel (copy sample one from left, then right. copy sample
                // two from left, then right, etc.)
                double sum = 0;

                for(int s = 0; s < 4; ++s) {

//                for (; i < data_size / data->channels; i += data->sample_size) {
                    for (c = 0; c < data->channels; c++) {
//                        if (populate_sample_buffer) {
//                            memcpy(data->samples + total_size, pFrame->extended_data[c] + i, data->sample_size);
//                        }
                        double sample = getSample(data->decoder_context, pFrame->extended_data[c], s);
                        if(sample < 0)
                            sum += -sample;
                        else
                            sum += sample;

                        // TODO:
                        // TODO:
                        // TODO:
                        // TODO:
                        // TODO:
                        // TODO:

                        total_size += data->sample_size;
                    }
                    int amplitude = pow(((int)(((sum * 2) / 4) * 100)), 0.5);
                    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Amplitude = %d\n", amplitude);
                }
            } else {
                /*
                double sum = 0;
                int c = 0;

                for(int s = 0; s < 4; ++s) {

//                for (; i < data_size / data->channels; i += data->sample_size) {
                    for (c = 0; c < data->channels; c++) {
//                        if (populate_sample_buffer) {
//                            memcpy(data->samples + total_size, pFrame->extended_data[c] + i, data->sample_size);
//                        }
                        double sample = getSample(data->decoder_context, pFrame->extended_data[c], s);
                        if(sample < 0)
                            sum += -sample;
                        else
                            sum += sample;

                        // TODO:
                        // TODO:
                        // TODO:
                        // TODO:
                        // TODO:
                        // TODO:

                        total_size += data->sample_size;
                    }
                    int amplitude = pow(((int)(((sum * 2) / 4) * 100)), 0.5);
                    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Amplitude = %d\n", amplitude);
                }

                __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "SAMPLES_EXISTS");*/

                // source file is already interleaved. just copy the raw data from the frame into
                // the `samples` buffer.
                if (populate_sample_buffer) {
                    memcpy(data->samples + total_size, pFrame->extended_data[0], data_size);
                }

                total_size += data_size;

//                for(int i = 0; i < total_size; i++) {
//                    int amplitude = pow(((int)(((data->samples[i] * 2) / 4) * 100)), 0.5);
//                    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "SAMPLES_EXISTS %d\n", amplitude);
//                }

            }
        }

        // Packets must be freed, otherwise you'll have a fix a hole where the rain gets in
        // (and keep your mind from wandering...)
        av_free_packet(&packet);
    }

    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Frames = %d\n", frames);

    data->size = total_size;
    data->sample_rate = raw_sample_rate;

    av_frame_free(&pFrame);

    if (total_size == 0) {
        // not a single packet could be read.
        return;
    }

    data->duration = (data->size * 8.0) / (raw_sample_rate * data->sample_size * 8.0 * data->channels);
}



/*
 * Take the given AudioData struct and convert all the compressed data
 * into the raw interleaved sample buffer.
 *
 * This function also calculates and populates the metadata information from
 * `read_audio_metadata`.
 */
void read_audio_data(AudioData *data) {
    read_raw_audio_data(data, 1);
}




/*
 * Take the given AudioData struct and calculate all of the properties
 * without doing any of the memory operations on the raw sample data.
 *
 * This is so we can get accurate metadata about the file (which we can't
 * really do for all formats unless we look at the raw data underneith, hence
 * why somethings ffmpeg isn't entirely accurate with duration via ffprobe)
 * without the overhead of image drawing.
 *
 * NOTE: data->samples will still not be valid after calling this function.
 * If you care about this information but also want data->samples to be
 * populated, use `read_audio_data` instead
 */
void read_audio_metadata(AudioData *data) {
    read_raw_audio_data(data, 0);
}


// take the given WaveformPNG struct and draw an audio waveform based on the data from
// the given AudioData struct. This function will combine all channels into a single waveform
// by averaging them.
void draw_combined_waveform(AudioData *data) {
//    int last_y = png->height - 1; // count of pixels in height starting from 0

    // figure out the min and max ranges of samples, based on bit depth and format
    int sample_min;
    int sample_max;

    get_format_range(data->format, &sample_min, &sample_max);

    uint32_t sample_range = sample_max - sample_min; // total range of values a sample can have
    int sample_count = data->size / data->sample_size; // how many samples are there total?
//    int samples_per_pixel = sample_count / png->width; // how many samples fit in a column of pixels?

    // multipliers used to produce averages while iterating through samples.
    double channel_average_multiplier = 1.0 / data->channels;

    // 10% padding
//    int padding = (int) (png->height * 0.05);
//    int track_height = png->height - (padding * 2);


    int x;
    int samples_per_pixel = 2;

    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Sample count = %d\n", sample_count);
    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Data size = %d\n", data->size);
    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Sample size = %d\n", data->sample_size);
    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Duration = %f\n", data->duration);


    /*for (x = 0; x < 1920; ++x) {
        // find the average sample value, the minimum sample value, and the maximum
        // sample value within the the range of samples that fit within this column of pixels
        double min = sample_max;
        double max = sample_min;

        //for each "sample", which is really a sample for each channel,
        //reduce the samples * channels value to a single value that is
        //the average of the samples for each channel.
        int i;
        for (i = 0; i < samples_per_pixel; i += data->channels) {
            double value = 0;

            int c;
            for (c = 0; c < data->channels; ++c) {
                int index = x * samples_per_pixel + i + c;

                value += get_sample(data, index) * channel_average_multiplier;
            }
//            __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Value = %f\n", value);


            if (value < min) {
                min = value;
            }

            if (value > max) {
                max = value;
            }
        }


        // calculate the y pixel values that represent the waveform for this column of pixels.
        // they are subtracted from last_y to flip the waveform image, putting positive
        // numbers above the center of waveform and negative numbers below.
//        float track_height = 500;
//        int y_max = track_height - ((min - sample_min) * track_height / sample_range);
//        int y_min = track_height - ((max - sample_min) * track_height / sample_range);



//        draw_column_segment(png, x, 0, last_y, y_min, y_max);
    }*/


    // for each column of pixels in the final output image
    /*int x;
    for (x = 0; x < png->width; ++x) {
        // find the average sample value, the minimum sample value, and the maximum
        // sample value within the the range of samples that fit within this column of pixels
        double min = sample_max;
        double max = sample_min;

        //for each "sample", which is really a sample for each channel,
        //reduce the samples * channels value to a single value that is
        //the average of the samples for each channel.
        int i;
        for (i = 0; i < samples_per_pixel; i += data->channels) {
            double value = 0;

            int c;
            for (c = 0; c < data->channels; ++c) {
                int index = x * samples_per_pixel + i + c;

                value += get_sample(data, index) * channel_average_multiplier;
            }

            if (value < min) {
                min = value;
            }

            if (value > max) {
                max = value;
            }
        }

        // calculate the y pixel values that represent the waveform for this column of pixels.
        // they are subtracted from last_y to flip the waveform image, putting positive
        // numbers above the center of waveform and negative numbers below.
        int y_max = track_height - ((min - sample_min) * track_height / sample_range) + padding;
        int y_min = track_height - ((max - sample_min) * track_height / sample_range) + padding;

        draw_column_segment(png, x, 0, last_y, y_min, y_max);
    }*/
}



extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
        ) {

    std::string filename = ConvertJString( env, audio_path );
    std::string resultFrame;



    int monofy = 1; // should we reduce everything into one waveform
    int metadata = 0; // should we just spit out metadata and not draw an image
//    const char *pFilePath = "/storage/9016-4EF8/MUSIC/Luttrell - What You Are.mp3"; // audio input file path
    const char *pFilePath = "/storage/emulated/0/Music/kygo.wav"; // audio input file path
//    const char *pOutFile = "/storage/9016-4EF8/MUSIC/Worakls - Red Dressed (Ben Böhmer Remix).mp3"; // image output file path. `NULL` means stdout


    // We could be fed any number of types of audio containers with any number of
    // encodings. These functions tell ffmpeg to load every library it knows about.
    // This way we don't need to explicity tell ffmpeg which libraries to load.
    //
    // Register all availible muxers/demuxers/protocols. We could be fed anything.
    av_register_all();

    // register all codecs/parsers/bitstream-filters
    avcodec_register_all();

    AVFormatContext *pFormatContext = NULL; // Container for the audio file
    AVCodecContext *pDecoderContext = NULL; // Container for the stream's codec
    AVCodec *pDecoder = NULL; // actual codec for the stream
    int stream_index = 0; // which audio stream should be looked at

    // open the audio file
    if (avformat_open_input(&pFormatContext, pFilePath, NULL, NULL) < 0) {
        fprintf(stderr, "Cannot open input file.\n");
        __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Cannot open input file");
        return env->NewStringUTF("Cannot open input file");
    }

    // Tell ffmpeg to read the file header and scan some of the data to determine
    // everything it can about the format of the file
    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {
        fprintf(stderr, "Cannot find stream information.\n");
        __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Cannot find stream information");
        return env->NewStringUTF("Cannot find stream information");
    }

    // find the audio stream we probably care about.
    // For audio files, there will most likely be only one stream.
    stream_index = av_find_best_stream(pFormatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &pDecoder, 0);

    if (stream_index < 0) {
        fprintf(stderr, "Unable to find audio stream in file.\n");
        __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Unable to find audio stream in file");
        return env->NewStringUTF("Unable to find audio stream in file");
    }

    // now that we have a stream, get the codec for the given stream
    pDecoderContext = pFormatContext->streams[stream_index]->codec;

    // open the decoder for this audio stream
    if (avcodec_open2(pDecoderContext, pDecoder, NULL) < 0) {
        fprintf(stderr, "Cannot open audio decoder.\n");
        __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "Cannot open audio decoder");
        return env->NewStringUTF("Cannot open audio decoder");
    }

    AudioData *data = create_audio_data_struct(pFormatContext, pDecoderContext);

    if (data == NULL) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "NULL data");
        return env->NewStringUTF("NULL data");
    }

    if (metadata) {
        // only fetch metadata about the file.
        read_audio_metadata(data);

        printf("    %-*s: %f seconds\n", 15, "Duration", data->duration);
        printf("    %-*s: %s\n", 15, "Compression", pDecoderContext->codec->name);
        printf("    %-*s: %i Hz\n", 15, "Sample rate", data->sample_rate);
        printf("    %-*s: %i\n", 15, "Channels", data->channels);
        printf("    %-*s: %i b/s\n", 15, "Bit rate", pFormatContext->bit_rate);
    } else {
        // fetch the raw data and the metadata
        read_audio_data(data);

        if (data->size == 0) {
            __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "0 data size");
            return env->NewStringUTF("0 data size");
        }

        // if there is both a height and track_height and track height * channels will fit within
        // height OR there is no height and we are not monofying:
        /*if ((track_height > 0 && height > 0 && track_height * data->channels < height) ||
            (height <= 0 && !monofy)) {
            // set the image height equal to track_height * channels
            height = track_height * data->channels;
        }*/

        draw_combined_waveform(data);

        /*// init the png struct so we can start drawing
        WaveformPNG png = init_png(pOutFile, width, height);

        if (monofy) {
            // if specified, call the drawing function that reduces all channels into a single
            // waveform
            draw_combined_waveform(&png, data);
        } else {
            // otherwise, draw them all stacked individually
            draw_waveform(&png, data);
        }

        write_png(&png);
        close_png(&png);*/
    }

    free_audio_data(data);
//    cleanup(pFormatContext, pDecoderContext);
    __android_log_print(ANDROID_LOG_VERBOSE, "AMPLITUDA_NDK_LOG", "SUCCESS!");
    return env->NewStringUTF("0");
}


/*

 #include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
}

std::string ConvertJString(JNIEnv* env, jstring str){
    if ( !str ) std::string();
    const jsize len = env->GetStringUTFLength(str);
    const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);
    std::string Result(strChars, len);
    env->ReleaseStringUTFChars(str, strChars);
    return Result;
}

float getSample(const AVCodecContext* codecCtx, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    float ret = 0;
    int sampleSize = av_get_bytes_per_sample(codecCtx->sample_fmt);

    switch(sampleSize) {
        case 1:
            val = (reinterpret_cast<uint8_t*>(buffer))[sampleIndex];
            val -= 127;
            break;
        case 2: val = (reinterpret_cast<uint16_t*>(buffer))[sampleIndex];
            break;
        case 4: val = (reinterpret_cast<uint32_t*>(buffer))[sampleIndex];
            break;
        case 8: val = (reinterpret_cast<uint64_t*>(buffer))[sampleIndex];
            break;
        default:
            return 0;
    }

    // Check which data type is in the sample.
    switch(codecCtx->sample_fmt) {
        case AV_SAMPLE_FMT_U8:
        case AV_SAMPLE_FMT_S16:
        case AV_SAMPLE_FMT_S32:
        case AV_SAMPLE_FMT_U8P:
        case AV_SAMPLE_FMT_S16P:
        case AV_SAMPLE_FMT_S32P:
            // integer => Scale to [-1, 1] and convert to float.
            ret = val / (static_cast<float>(((1 << (sampleSize*8-1))-1)));
            break;
        case AV_SAMPLE_FMT_FLT:
        case AV_SAMPLE_FMT_FLTP:
            // float => reinterpret
            ret = *reinterpret_cast<float*>(&val);
            break;
        case AV_SAMPLE_FMT_DBL:
        case AV_SAMPLE_FMT_DBLP:
            // double => reinterpret and then static cast down
            ret = static_cast<float>(*reinterpret_cast<double*>(&val));
            break;
        default:
            return 0;
    }

    return ret;

}

extern "C" JNIEXPORT jstring JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
        ) {

    std::string filename = ConvertJString( env, audio_path );
    std::string resultFrame;

    av_register_all();

    AVFrame* frame = av_frame_alloc();

    if (!frame){
        return env->NewStringUTF("Error allocating the frame! Amplituda:exception:600");
    }

    AVFormatContext* formatContext = NULL;
    if (avformat_open_input(&formatContext, filename.data(), NULL, NULL) != 0) {
        av_free(frame);
        // FIXME: error with same file form waveformSeekBar
        return env->NewStringUTF("Error opening file, file path is not valid! Amplituda:exception:800");
    }

    if (avformat_find_stream_info(formatContext, NULL) < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Error finding the stream information! Amplituda:exception:57005");
    }

    AVCodec* cdc = nullptr;
    int streamIndex = av_find_best_stream(formatContext, AVMEDIA_TYPE_AUDIO, -1, -1, &cdc, 0);
    if (streamIndex < 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Could not find any audio stream in the file! Amplituda:exception:165");
    }

    AVStream* audioStream = formatContext->streams[streamIndex];
    AVCodecContext* codecContext = audioStream->codec;
    codecContext->codec = cdc;

    if (avcodec_open2(codecContext, codecContext->codec, NULL) != 0){
        av_free(frame);
        avformat_close_input(&formatContext);
        return env->NewStringUTF("Couldn't open the context with the decoder! (can't find or open context) Amplituda:exception:1057");
    }

    AVPacket readingPacket;
    av_init_packet(&readingPacket);

    while (av_read_frame(formatContext, &readingPacket) == 0)
    {
        if (readingPacket.stream_index == audioStream->index)
        {
            AVPacket decodingPacket = readingPacket;
            while (decodingPacket.size > 0)
            {
                int gotFrame = 0;
                int result = avcodec_decode_audio4(codecContext, frame, &gotFrame, &decodingPacket);

                if (result >= 0 && gotFrame)
                {
                    decodingPacket.size -= result;
                    decodingPacket.data += result;

                    float sum = 0;
                    for(int s = 0; s < 4; ++s) {
                        for(int c = 0; c < codecContext->channels; ++c) {
                            float sample = getSample(codecContext, frame->extended_data[c], s);
                            if(sample < 0)
                                sum += -sample;
                            else
                                sum += sample;
                        }
                    }
//                    float average_point = (sum * 2) / 4;

//                    int amplitude = pow(((int)(average_point * 100)), 0.5);
                    int amplitude = pow(((int)(((sum * 2) / 4) * 100)), 0.5);

                    resultFrame.append(std::to_string(  amplitude ));
                    resultFrame.append("\n");
                }
                else
                {
                    decodingPacket.size = 0;
                    decodingPacket.data = nullptr;
                }
            }
        }

        av_free_packet(&readingPacket);
    }

    av_free(frame);
    avcodec_close(codecContext);
    avformat_close_input(&formatContext);
    return env->NewStringUTF(resultFrame.data());
}

*/