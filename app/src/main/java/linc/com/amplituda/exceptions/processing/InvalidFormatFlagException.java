package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.INVALID_FORMAT_FLAG_PROC_CODE;

public final class InvalidFormatFlagException extends AmplitudaProcessingException {
    public InvalidFormatFlagException() {
        super("Invalid format passed in the parameters! Please use Amplituda constants", INVALID_FORMAT_FLAG_PROC_CODE);
    }
}
