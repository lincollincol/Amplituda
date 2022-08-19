package linc.com.amplituda.exceptions.io;

import static linc.com.amplituda.ErrorCode.INVALID_AUDIO_BYTE_ARRAY_IO_CODE;

public final class InvalidAudioByteArrayException extends AmplitudaIOException {
    public InvalidAudioByteArrayException() {
        super("Invalid audio byte array. Could not use byte array as input audio!", INVALID_AUDIO_BYTE_ARRAY_IO_CODE);
    }
}