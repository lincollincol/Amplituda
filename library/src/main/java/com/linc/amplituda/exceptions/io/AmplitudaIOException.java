package com.linc.amplituda.exceptions.io;

import com.linc.amplituda.exceptions.AmplitudaException;

public class AmplitudaIOException extends AmplitudaException {
    public AmplitudaIOException(String message, final int code) {
        super(message, code);
    }
}
