package linc.com.amplituda.exceptions.io;

public final class CodecNotFoundException extends AmplitudaIOException {
    public CodecNotFoundException() {
        super("Failed to find codec!");
    }
}
