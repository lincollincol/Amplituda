package linc.com.amplituda.exceptions.allocation;

import linc.com.amplituda.exceptions.AmplitudaException;

public class AmplitudaAllocationException extends AmplitudaException {
    public AmplitudaAllocationException(String message, final int code) {
        super(message, code);
    }
}
