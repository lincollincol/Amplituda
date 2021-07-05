package linc.com.amplituda.exceptions.allocation;

import static linc.com.amplituda.ErrorCode.FRAME_ALLOC_CODE;

public final class FrameAllocationException extends AmplitudaAllocationException {
    public FrameAllocationException() {
        super("Could not allocate frame!", FRAME_ALLOC_CODE);
    }
}
