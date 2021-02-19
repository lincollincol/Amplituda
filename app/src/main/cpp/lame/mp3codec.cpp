#include <jni.h>
#include "lameutils/mp3file_encoder.h"
#include "saka_log.h"

mp3file_encoder *encoder;
extern "C"
JNIEXPORT jint JNICALL
Java_linc_com_pcmdecoder_PCMDecoder_init(JNIEnv *env, jclass type, jstring pcmFilePath_,
                                  jint audioChannels, jint bitRate, jint sampleRate,
                                  jstring mp3FilePath_) {
    const char *pcmFilePath = env->GetStringUTFChars(pcmFilePath_, 0);
    const char *mp3FilePath = env->GetStringUTFChars(mp3FilePath_, 0);
    SAKA_LOG_DEBUG("pcm=%s", pcmFilePath);
    SAKA_LOG_DEBUG("mp3=%s", mp3FilePath);
    encoder = new mp3file_encoder();
    encoder->Init(pcmFilePath, mp3FilePath, sampleRate, audioChannels, bitRate);
    env->ReleaseStringUTFChars(pcmFilePath_, pcmFilePath);
    env->ReleaseStringUTFChars(mp3FilePath_, mp3FilePath);
    return 1;
}

extern "C"
JNIEXPORT void JNICALL
Java_linc_com_pcmdecoder_PCMDecoder_encode(JNIEnv *env, jclass type) {

    encoder->Encode();

}
extern "C"
JNIEXPORT void JNICALL
Java_linc_com_pcmdecoder_PCMDecoder_destroy(JNIEnv *env, jclass type) {

    encoder->Destroy();

}