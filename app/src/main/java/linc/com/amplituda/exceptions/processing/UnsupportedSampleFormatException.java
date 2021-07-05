package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.UNSUPPORTED_SAMPLE_FMT_PROC_CODE;

public final class UnsupportedSampleFormatException extends AmplitudaProcessingException {
    public UnsupportedSampleFormatException() {
        super("Sample format is not supported!", UNSUPPORTED_SAMPLE_FMT_PROC_CODE);
    }
}
