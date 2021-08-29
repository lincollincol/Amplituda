cmake_minimum_required(VERSION 3.4.1)

set(ffmpeg_DIR ${CMAKE_SOURCE_DIR}/ffmpeg)

include_directories(${ffmpeg_DIR}/${ANDROID_ABI}/include)

add_library(avutil SHARED IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavutil.so)

add_library(avformat SHARED IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavformat.so)

add_library(avcodec SHARED IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavcodec.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libswresample.so)

add_library(native-lib SHARED native-lib.cpp)
set_target_properties(native-lib PROPERTIES LINKER_LANGUAGE CXX)

find_library( log-lib log )

target_link_libraries(native-lib
        avutil
        avformat
        avcodec
        swresample
        ${log-lib}
        )