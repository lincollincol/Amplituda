cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_FLAGE "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -Werror")

set(LIB_MP3 Mp3Codec)
include_directories(
        src/main/cpp/include
)

add_subdirectory(libmp3lame)

add_subdirectory(lameutils)

add_library(
        ${LIB_MP3}
        SHARED
        mp3codec.cpp
)

find_library(
        log-lib
        log )

target_link_libraries(${LIB_MP3}
        ${log-lib}
        #mp3lame
        lame_utils
        )

