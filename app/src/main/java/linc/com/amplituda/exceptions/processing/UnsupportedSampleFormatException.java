package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.UNSUPPORTED_SAMPLE_FMT;

public class UnsupportedSampleFormatException extends AmplitudaProcessingException {
    public UnsupportedSampleFormatException() {
        super("Sample format is not supported!", UNSUPPORTED_SAMPLE_FMT);
    }
}
