package linc.com.amplituda.exceptions.io;

public final class NoInputFileException extends AmplitudaIOException {
    public NoInputFileException() {
        super("Input file not found! Please call fromFile() or fromPath() at first!");
    }
}
