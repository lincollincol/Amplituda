package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.NOT_FOUND_CODEC;

public final class CodecNotFoundException extends AmplitudaProcessingException {
    public CodecNotFoundException() {
        super("Failed to find codec!", NOT_FOUND_CODEC);
    }
}
