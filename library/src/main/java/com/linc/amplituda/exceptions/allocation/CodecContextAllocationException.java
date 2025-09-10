package com.linc.amplituda.exceptions.allocation;

import static com.linc.amplituda.ErrorCode.CODEC_CONTEXT_ALLOC_CODE;

public final class CodecContextAllocationException extends AmplitudaAllocationException {
    public CodecContextAllocationException() {
        super("Failed to allocate the codec context!", CODEC_CONTEXT_ALLOC_CODE);
    }
}
