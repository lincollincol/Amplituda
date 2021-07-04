package linc.com.amplituda.exceptions.io;

import linc.com.amplituda.exceptions.AmplitudaException;

public class AmplitudaIOException extends AmplitudaException {
    public AmplitudaIOException(String message, final int code) {
        super(message, code);
    }
}
