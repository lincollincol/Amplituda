package linc.com.amplituda.exceptions.io;

public final class StreamInformationNotFoundException extends AmplitudaIOException {
    public StreamInformationNotFoundException() {
        super("Could not find stream information!");
    }
}
