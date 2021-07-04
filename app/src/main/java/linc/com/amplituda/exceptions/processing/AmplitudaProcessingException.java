package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.AmplitudaException;

public class AmplitudaProcessingException extends AmplitudaException {
    public AmplitudaProcessingException(String message, final int code) {
        super(message, code);
    }
}
