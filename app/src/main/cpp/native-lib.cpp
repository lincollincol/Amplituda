#include <jni.h>
//#include <android/log.h>
#include <string>
#include "error_code.h"

extern "C" {
#include "libavutil/timestamp.h"
#include "libavutil/samplefmt.h"
#include "libavformat/avformat.h"
}

static AVFormatContext *fmt_ctx = NULL;
static AVCodecContext *audio_dec_ctx;
static AVStream *audio_stream = NULL;

static int audio_stream_idx = -1;
static AVFrame *frame = NULL;
static AVPacket *pkt = NULL;

void add_error(std::string* errors, const int code) {
    *errors += std::to_string(code);
    *errors += " ";
}

double getSample(const AVCodecContext* codecCtx, uint8_t* buffer, int sampleIndex) {
    int64_t val = 0;
    double ret = 0;
    int sampleSize = av_get_bytes_per_sample(codecCtx->sample_fmt);

    switch(sampleSize) {
        case 1:
            val = (reinterpret_cast<uint8_t*>(buffer))[sampleIndex];
            val -= 127;
            break;
        case 2:
            val = (reinterpret_cast<int16_t*>(buffer))[sampleIndex];
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
            ret = *reinterpret_cast<double*>(&val);
            break;
        default:
            return 0;
    }

    return ret;
}

static int decode_packet(AVCodecContext *dec, const AVPacket *pkt, std::string* result, std::string* errors)
{
    int ret = 0;

    // submit the packet to the decoder
    ret = avcodec_send_packet(dec, pkt);
    if (ret < 0) {
        add_error(errors, PACKET_SUBMITTING_ERR);
        return ret;
    }

    // get all the available frames from the decoder
    while (ret >= 0) {
        ret = avcodec_receive_frame(dec, frame);
        if (ret < 0) {
            // those two return values are special and mean there is no output
            // frame available, but there were no errors during decoding
            if (ret == AVERROR_EOF || ret == AVERROR(EAGAIN)) {
                return 0;
            }
            add_error(errors, DECODING_ERR);
            return ret;
        }

        // write the frame data to output file
        if(dec->codec->type == AVMEDIA_TYPE_AUDIO) {
            double sum = 0;


            for(int i = 0; i < frame->nb_samples; i++) {
                double sample = getSample(audio_dec_ctx, frame->data[0], i);
                sum += sample * sample;
            }

            (*result) += std::to_string(((int)(sqrt(sum / frame->nb_samples) * 100)));
            (*result) += "\n";
        }

        av_frame_unref(frame);
        if (ret < 0) {
            return ret;
        }
    }

    return 0;
}

static int open_codec_context(
        int *stream_idx,
        AVCodecContext **dec_ctx,
        AVFormatContext *fmt_ctx,
        enum AVMediaType type,
        std::string* errors
) {
    int ret, stream_index;
    AVStream *st;
    const AVCodec *dec = NULL;
    AVDictionary *opts = NULL;

    ret = av_find_best_stream(fmt_ctx, type, -1, -1, NULL, 0);
    if (ret < 0) {
        add_error(errors, NOT_FOUND_STREAM);
        return ret;
    } else {
        stream_index = ret;
        st = fmt_ctx->streams[stream_index];

        /* find decoder for the stream */
        dec = avcodec_find_decoder(st->codecpar->codec_id);
        if (!dec) {
            add_error(errors, NOT_FOUND_CODEC);
            return AVERROR(EINVAL);
        }

        /* Allocate a codec context for the decoder */
        *dec_ctx = avcodec_alloc_context3(dec);
        if (!*dec_ctx) {
            add_error(errors, ALLOC_CODEC_CTX_ERR);
            return AVERROR(ENOMEM);
        }

        /* Copy codec parameters from input stream to output codec context */
        if ((ret = avcodec_parameters_to_context(*dec_ctx, st->codecpar)) < 0) {
            add_error(errors, CODEC_PARAMETERS_ERR);
            return ret;
        }

        /* Init the decoders */
        if ((ret = avcodec_open2(*dec_ctx, dec, &opts)) < 0) {
            add_error(errors, CODEC_OPEN_ERR);
            return ret;
        }
        *stream_idx = stream_index;
    }

    return 0;
}

static int get_format_from_sample_fmt(const char **fmt, enum AVSampleFormat sample_fmt) {
    int i;
    struct sample_fmt_entry {
        enum AVSampleFormat sample_fmt;
        const char *fmt_be, *fmt_le;
    } sample_fmt_entries[] = {
            { AV_SAMPLE_FMT_U8,  "u8",    "u8"    },
            { AV_SAMPLE_FMT_S16, "s16be", "s16le" },
            { AV_SAMPLE_FMT_S32, "s32be", "s32le" },
            { AV_SAMPLE_FMT_FLT, "f32be", "f32le" },
            { AV_SAMPLE_FMT_DBL, "f64be", "f64le" },
    };
    *fmt = NULL;

    for (i = 0; i < FF_ARRAY_ELEMS(sample_fmt_entries); i++) {
        struct sample_fmt_entry *entry = &sample_fmt_entries[i];
        if (sample_fmt == entry->sample_fmt) {
            *fmt = AV_NE(entry->fmt_be, entry->fmt_le);
            return 0;
        }
    }

    return -1;
}

extern "C" JNIEXPORT jobject JNICALL

Java_linc_com_amplituda_Amplituda_amplitudesFromAudioJNI(
        JNIEnv* env,
        jobject,
        jstring audio_path
) {
    const char* input_audio = env->GetStringUTFChars(audio_path, 0);
    int ret = 0;

    // Return wrapper class
    jclass amplitudaResultClass = (env)->FindClass("linc/com/amplituda/AmplitudaResultJNI");
    jmethodID constructor = (env)->GetMethodID(amplitudaResultClass, "<init>", "()V");

    // Wrapper fields
    jfieldID amplitudes = (env)->GetFieldID(amplitudaResultClass, "amplitudes", "Ljava/lang/String;");
    jfieldID errors = (env)->GetFieldID(amplitudaResultClass, "errors", "Ljava/lang/String;");

    // Create wrapper object
    jobject amplitudaResultReturnObject = (env)->NewObject(amplitudaResultClass, constructor);

    std::string amplitudes_data = "";
    std::string errors_data = "";

    /*add_error(&errors_data, NOT_FOUND_CODEC);
    add_error(&errors_data, NOT_FOUND_STREAM_INFO);
    add_error(&errors_data, ALLOC_CODEC_CTX_ERR);
    goto end_return;*/

    // open input file, and allocate format context
    if (avformat_open_input(&fmt_ctx, input_audio, NULL, NULL) < 0) {
        add_error(&errors_data, FILE_OPEN_ERR);
        goto end_return;
    }

    // retrieve stream information
    if (avformat_find_stream_info(fmt_ctx, NULL) < 0) {
        add_error(&errors_data, NOT_FOUND_STREAM_INFO);
        goto end_return;
    }

    if (open_codec_context(&audio_stream_idx, &audio_dec_ctx, fmt_ctx, AVMEDIA_TYPE_AUDIO, &errors_data) >= 0) {
        audio_stream = fmt_ctx->streams[audio_stream_idx];
    }

    // dump input information to stderr
    av_dump_format(fmt_ctx, 0, input_audio, 0);

    if (!audio_stream) {
        ret = 1;
        add_error(&errors_data, NOT_FOUND_STREAM);
        goto end_cleanup;
    }

    frame = av_frame_alloc();
    if (!frame) {
        ret = AVERROR(ENOMEM);
        add_error(&errors_data, ALLOC_FRAME_ERR);
        goto end_cleanup;
    }

    pkt = av_packet_alloc();
    if (!pkt) {
        ret = AVERROR(ENOMEM);
        add_error(&errors_data, ALLOC_PACKET_ERR);
        goto end_cleanup;
    }

    // read frames from the file
    while (av_read_frame(fmt_ctx, pkt) >= 0) {
        // check if the packet belongs to a stream we are interested in, otherwise skip it
        if (pkt->stream_index == audio_stream_idx) {
            ret = decode_packet(audio_dec_ctx, pkt, &amplitudes_data, &errors_data);
        }

        av_packet_unref(pkt);
        if (ret < 0)
            break;
    }

    // flush the decoders
    if (audio_dec_ctx)
        decode_packet(audio_dec_ctx, NULL, &amplitudes_data, &errors_data);

    if (audio_stream) {
        enum AVSampleFormat sfmt = audio_dec_ctx->sample_fmt;
        int n_channels = audio_dec_ctx->channels;
        const char *fmt;

        if (av_sample_fmt_is_planar(sfmt)) {
            const char *packed = av_get_sample_fmt_name(sfmt);
            sfmt = av_get_packed_sample_fmt(sfmt);
            n_channels = 1;
        }

        if ((ret = get_format_from_sample_fmt(&fmt, sfmt)) < 0) {
            add_error(&errors_data, UNSUPPORTED_SAMPLE_FMT);
            goto end_cleanup;
        }
    }

    // Release ffmpeg data
    end_cleanup:

    avcodec_free_context(&audio_dec_ctx);
    avformat_close_input(&fmt_ctx);
    av_packet_free(&pkt);
    av_frame_free(&frame);

    // Return without ffmpeg release
    end_return:

    env->ReleaseStringUTFChars(audio_path, input_audio);

    (env)->SetObjectField(amplitudaResultReturnObject, amplitudes, (env)->NewStringUTF(amplitudes_data.c_str()));
    (env)->SetObjectField(amplitudaResultReturnObject, errors, (env)->NewStringUTF(errors_data.c_str()));

    return amplitudaResultReturnObject;
}