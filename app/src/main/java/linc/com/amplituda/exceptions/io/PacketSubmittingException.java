package linc.com.amplituda.exceptions.io;

public final class PacketSubmittingException extends AmplitudaIOException {
    public PacketSubmittingException() {
        super("Error submitting a packet for decoding!");
    }
}
