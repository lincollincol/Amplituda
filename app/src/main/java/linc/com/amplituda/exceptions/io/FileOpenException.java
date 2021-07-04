package linc.com.amplituda.exceptions.io;

public final class FileOpenException extends AmplitudaIOException {
    public FileOpenException() {
        super("Could not open input file!");
    }
}
