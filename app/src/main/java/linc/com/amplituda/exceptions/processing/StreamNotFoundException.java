package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.NOT_FOUND_STREAM;

public final class StreamNotFoundException extends AmplitudaProcessingException {
    public StreamNotFoundException() {
        super("Could not find stream in the input file!", NOT_FOUND_STREAM);
    }
}
