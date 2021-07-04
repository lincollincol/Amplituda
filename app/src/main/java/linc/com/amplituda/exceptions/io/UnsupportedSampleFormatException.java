package linc.com.amplituda.exceptions.io;

public class UnsupportedSampleFormatException extends AmplitudaIOException {
    public UnsupportedSampleFormatException() {
        super("Sample format is not supported!");
    }
}
