package com.linc.amplituda.exceptions.io;

import static com.linc.amplituda.ErrorCode.NO_INPUT_FILE_IO_CODE;

public final class NoInputFileException extends AmplitudaIOException {
    public NoInputFileException() {
        super("Input file not found! Please call fromFile() or fromPath() at first!", NO_INPUT_FILE_IO_CODE);
    }
}
