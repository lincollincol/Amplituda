package com.linc.amplituda.exceptions.allocation;

import com.linc.amplituda.exceptions.AmplitudaException;

public class AmplitudaAllocationException extends AmplitudaException {
    public AmplitudaAllocationException(String message, final int code) {
        super(message, code);
    }
}
