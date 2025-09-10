package com.linc.amplituda.exceptions.allocation;

import static com.linc.amplituda.ErrorCode.FRAME_ALLOC_CODE;

public final class FrameAllocationException extends AmplitudaAllocationException {
    public FrameAllocationException() {
        super("Could not allocate frame!", FRAME_ALLOC_CODE);
    }
}
