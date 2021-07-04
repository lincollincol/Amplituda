package linc.com.amplituda.exceptions.io;

import static linc.com.amplituda.ErrorCode.INVALID_RAW_RESOURCE_ERR;

public final class InvalidRawResourceException extends AmplitudaIOException {
    public InvalidRawResourceException() {
        super("Invalid raw resource. Could not use file as input audio!", INVALID_RAW_RESOURCE_ERR);
    }
}
