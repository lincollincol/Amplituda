package linc.com.amplituda.exceptions.io;

public final class FileNotFoundException extends AmplitudaIOException {
    public FileNotFoundException() {
        super("Input file not found! Wrong file path or file does not exist!");
    }
}
