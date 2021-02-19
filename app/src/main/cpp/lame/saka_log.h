#ifndef VIDEOPLAY_SAKA_LOG_H
#define VIDEOPLAY_SAKA_LOG_H

#endif //VIDEOPLAY_SAKA_LOG_H

#include <android/log.h>
//#mp3codec.include "config.h"

#define SAKA_LOG_LEVEL_VERBOSE 1
#define SAKA_LOG_LEVEL_DEBUG 2
#define SAKA_LOG_LEVEL_INFO 3
#define SAKA_LOG_LEVEL_WARN 4
#define SAKA_LOG_LEVEL_ERROR 5
#define SAKA_LOG_LEVEL_FATAL 6
#define SAKA_LOG_LEVEL_SILENT 7
#ifndef SAKA_LOG_TAG
#define SAKA_LOG_TAG "NDK-TAG"
#endif
#ifndef SAKA_LOG_LEVEL
#define SAKA_LOG_LEVEL SAKA_LOG_LEVEL_VERBOSE
#endif

#define SAKA_LOG_NOOP (void) 0

#if LOG_INFO_LEVEL == 0
#define SAKA_LOG_PRINT(level, fmt, ...) \
        __android_log_print(level,SAKA_LOG_TAG,fmt, \
        ##__VA_ARGS__)

#elif LOG_INFO_LEVEL == 1
#define SAKA_LOG_PRINT(level, fmt, ...) \
        __android_log_print(level,SAKA_LOG_TAG,"%s: "fmt, \
        __PRETTY_FUNCTION__,##__VA_ARGS__)
#elif LOG_INFO_LEVEL == 2

#define SAKA_LOG_PRINT(level, fmt, ...) \
        __android_log_print(level,SAKA_LOG_TAG,"%s:%u: "fmt, \
        __FILE__,__LINE__,##__VA_ARGS__)


#elif LOG_INFO_LEVEL == 3
#define SAKA_LOG_PRINT(level, fmt, ...) \
        __android_log_print(level,SAKA_LOG_TAG,"(%s:%u) %s: "fmt, \
        __FILE__,__LINE__,__PRETTY_FUNCTION__,##__VA_ARGS__)
#else
#define SAKA_LOG_PRINT(level, fmt, ...) \
        __android_log_print(level,SAKA_LOG_TAG,"(%s:%u) %s: "fmt, \
        __FILE__,__LINE__,__PRETTY_FUNCTION__,##__VA_ARGS__)
#endif


#if SAKA_LOG_LEVEL_VERBOSE >= SAKA_LOG_LEVEL
#define SAKA_LOG_VERBOSE(fmt, ...) \
        SAKA_LOG_PRINT(ANDROID_LOG_VERBOSE,fmt,##__VA_ARGS__)
#else
#define SAKA_LOG_VERBOSE(...) SAKA_LOG_NOOP
#endif

#if SAKA_LOG_LEVEL_DEBUG >= SAKA_LOG_LEVEL
#define SAKA_LOG_DEBUG(fmt, ...) \
        SAKA_LOG_PRINT(ANDROID_LOG_DEBUG,fmt,##__VA_ARGS__)
#else
#define SAKA_LOG_DEBUG(...) SAKA_LOG_NOOP
#endif

#if SAKA_LOG_LEVEL_INFO >= SAKA_LOG_LEVEL
#define SAKA_LOG_INFO(fmt, ...) \
        SAKA_LOG_PRINT(ANDROID_LOG_INFO,fmt,##__VA_ARGS__)
#else
#define SAKA_LOG_INFO(...) SAKA_LOG_NOOP
#endif

#if SAKA_LOG_LEVEL_WARN >= SAKA_LOG_LEVEL
#define SAKA_LOG_WARN(fmt, ...) \
        SAKA_LOG_PRINT(ANDROID_LOG_WARN,fmt,##__VA_ARGS__)
#else
#define SAKA_LOG_WARN(...) SAKA_LOG_NOOP
#endif

#if SAKA_LOG_LEVEL_ERROR >= SAKA_LOG_LEVEL
#define SAKA_LOG_ERROR(fmt, ...) \
        SAKA_LOG_PRINT(ANDROID_LOG_ERROR,fmt,##__VA_ARGS__)
#else
#define SAKA_LOG_ERROR(...) SAKA_LOG_NOOP
#endif

#if SAKA_LOG_LEVEL_FATAL >= SAKA_LOG_LEVEL
#define SAKA_LOG_FATAL(fmt, ...) \
        SAKA_LOG_PRINT(ANDROID_LOG_FATAL,fmt,##__VA_ARGS__)
#else
#define SAKA_LOG_FATAL(...) SAKA_LOG_NOOP
#endif

#if SAKA_LOG_LEVEL_FATAL >= SAKA_LOG_LEVEL
#define SAKA_LOG_ASSERT(expression, fmt, ...) \
        if(!(expresiion)) \
        { \
                __android_log_assert(#expression,MY_LOG_TAG, \
                fmt,##__VA_ARGS__); \
        }
#else
#define SAKA_LOG_assert(...) SAKA_LOG_NOOP
#endif