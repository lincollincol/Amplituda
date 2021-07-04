package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.CODEC_OPEN_ERR;

public final class CodecOpenException extends AmplitudaProcessingException {
    public CodecOpenException() {
        super("Failed to open codec!", CODEC_OPEN_ERR);
    }
}
