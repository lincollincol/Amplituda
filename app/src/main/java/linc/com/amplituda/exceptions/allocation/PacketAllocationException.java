package linc.com.amplituda.exceptions.allocation;

public final class PacketAllocationException extends AmplitudaAllocationException {
    public PacketAllocationException() {
        super("Could not allocate packet!");
    }
}
