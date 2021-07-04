package linc.com.amplituda.exceptions.allocation;

import static linc.com.amplituda.ErrorCode.ALLOC_FRAME_ERR;

public final class FrameAllocationException extends AmplitudaAllocationException {
    public FrameAllocationException() {
        super("Could not allocate frame!", ALLOC_FRAME_ERR);
    }
}
