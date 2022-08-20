cmake_minimum_required(VERSION 3.4.1)

set(ffmpeg_DIR ${CMAKE_SOURCE_DIR}/ffmpeg)

include_directories(${ffmpeg_DIR}/${ANDROID_ABI}/include)

add_library(amplituda-avutil SHARED IMPORTED)
set_target_properties(amplituda-avutil PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavutil-amplituda.so)

add_library(amplituda-avformat SHARED IMPORTED)
set_target_properties(amplituda-avformat PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavformat-amplituda.so)

add_library(amplituda-avcodec SHARED IMPORTED)
set_target_properties(amplituda-avcodec PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavcodec-amplituda.so)

add_library(amplituda-swresample SHARED IMPORTED)
set_target_properties(amplituda-swresample PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libswresample-amplituda.so)

add_library(amplituda-native-lib SHARED amplituda-native-lib.cpp)
set_target_properties(amplituda-native-lib PROPERTIES LINKER_LANGUAGE CXX)

#find_library( log-lib log )

target_link_libraries(
        amplituda-native-lib
        amplituda-avutil
        amplituda-avformat
        amplituda-avcodec
        amplituda-swresample
#        ${log-lib}
)