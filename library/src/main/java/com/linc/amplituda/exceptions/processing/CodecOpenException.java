package com.linc.amplituda.exceptions.processing;

import static com.linc.amplituda.ErrorCode.CODEC_OPEN_PROC_CODE;

public final class CodecOpenException extends AmplitudaProcessingException {
    public CodecOpenException() {
        super("Failed to open codec!", CODEC_OPEN_PROC_CODE);
    }
}
