package linc.com.amplituda.exceptions.io;

public final class CodecParametersException extends AmplitudaIOException {
    public CodecParametersException() {
        super("Failed to copy codec parameters to decoder context!");
    }
}
