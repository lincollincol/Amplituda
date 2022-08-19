package linc.com.amplituda.exceptions.io;

import static linc.com.amplituda.ErrorCode.INVALID_AUDIO_INPUT_STREAM_IO_CODE;

public final class InvalidAudioInputStreamException extends AmplitudaIOException {
    public InvalidAudioInputStreamException() {
        super("Invalid audio input stream. Could not use input stream as input audio!", INVALID_AUDIO_INPUT_STREAM_IO_CODE);
    }
}