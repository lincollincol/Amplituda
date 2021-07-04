package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.DECODING_ERR;

public final class DecodingException extends AmplitudaProcessingException {
    public DecodingException() {
        super("Error during decoding!", DECODING_ERR);
    }
}
