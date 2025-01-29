package com.linc.amplituda.exceptions.processing;

import com.linc.amplituda.exceptions.AmplitudaException;

public class AmplitudaProcessingException extends AmplitudaException {
    public AmplitudaProcessingException(String message, final int code) {
        super(message, code);
    }
}
