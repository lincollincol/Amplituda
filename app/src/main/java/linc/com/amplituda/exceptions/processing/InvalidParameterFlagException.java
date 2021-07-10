package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.INVALID_PARAMETER_FLAG_PROC_CODE;

public final class InvalidParameterFlagException extends AmplitudaProcessingException {
    public InvalidParameterFlagException() {
        super("Invalid format passed in the parameters! Please read documentation", INVALID_PARAMETER_FLAG_PROC_CODE);
    }
}
