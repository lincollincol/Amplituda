package linc.com.amplituda.exceptions.io;

public final class StreamNotFoundException extends AmplitudaIOException {
    public StreamNotFoundException() {
        super("Could not find stream in the input file!");
    }
}
