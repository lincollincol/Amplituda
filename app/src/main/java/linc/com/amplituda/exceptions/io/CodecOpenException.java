package linc.com.amplituda.exceptions.io;

public final class CodecOpenException extends  AmplitudaIOException {
    public CodecOpenException() {
        super("Failed to open codec!");
    }
}
