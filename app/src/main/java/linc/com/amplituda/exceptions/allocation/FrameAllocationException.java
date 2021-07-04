package linc.com.amplituda.exceptions.allocation;

public final class FrameAllocationException extends AmplitudaAllocationException {
    public FrameAllocationException() {
        super("Could not allocate frame!");
    }
}
