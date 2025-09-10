package com.linc.amplituda.exceptions.io;

import static com.linc.amplituda.ErrorCode.FILE_OPEN_IO_CODE;

public final class FileOpenException extends AmplitudaIOException {
    public FileOpenException() {
        super("Could not open input file!", FILE_OPEN_IO_CODE);
    }
}
