package com.linc.amplituda.exceptions.processing;

import static com.linc.amplituda.ErrorCode.INVALID_PARAMETER_FLAG_PROC_CODE;

public final class InvalidParameterFlagException extends AmplitudaProcessingException {
    public InvalidParameterFlagException() {
        super("Invalid format passed in the parameters! Please read documentation", INVALID_PARAMETER_FLAG_PROC_CODE);
    }
}
