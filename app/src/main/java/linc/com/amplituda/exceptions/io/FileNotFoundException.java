package linc.com.amplituda.exceptions.io;

import static linc.com.amplituda.ErrorCode.NOT_FOUND_FILE;

public final class FileNotFoundException extends AmplitudaIOException {
    public FileNotFoundException() {
        super("Input file not found! Wrong file path or file does not exist!", NOT_FOUND_FILE);
    }
}
