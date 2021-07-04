package linc.com.amplituda.exceptions.allocation;

public final class CodecContextAllocationException extends AmplitudaAllocationException {
    public CodecContextAllocationException() {
        super("Failed to allocate the codec context!");
    }
}
