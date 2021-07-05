package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.DECODING_PROC_CODE;

public final class DecodingException extends AmplitudaProcessingException {
    public DecodingException() {
        super("Error during decoding!", DECODING_PROC_CODE);
    }
}
