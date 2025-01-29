package com.linc.amplituda.exceptions.io;

import static com.linc.amplituda.ErrorCode.INVALID_AUDIO_URL_IO_CODE;

public final class InvalidAudioUrlException extends AmplitudaIOException {
    public InvalidAudioUrlException() {
        super("Invalid audio file URL. Could not use file as input audio!", INVALID_AUDIO_URL_IO_CODE);
    }
}