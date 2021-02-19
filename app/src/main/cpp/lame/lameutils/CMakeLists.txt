add_library(lame_utils lame_utils.cpp mp3file_encoder.cpp wav_tools.cpp wavfile_encoder.cpp)
target_link_libraries(lame_utils
                      mp3lame)