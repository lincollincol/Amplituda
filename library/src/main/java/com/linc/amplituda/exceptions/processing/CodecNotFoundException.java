package com.linc.amplituda.exceptions.processing;

import static com.linc.amplituda.ErrorCode.CODEC_NOT_FOUND_PROC_CODE;

public final class CodecNotFoundException extends AmplitudaProcessingException {
    public CodecNotFoundException() {
        super("Failed to find codec!", CODEC_NOT_FOUND_PROC_CODE);
    }
}
