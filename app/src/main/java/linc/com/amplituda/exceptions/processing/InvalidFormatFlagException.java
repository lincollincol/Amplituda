package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.AmplitudaException;

import static linc.com.amplituda.ErrorCode.INVALID_FORMAT_FLAG_ERR;

public class InvalidFormatFlagException extends AmplitudaProcessingException {
    public InvalidFormatFlagException() {
        super("Invalid format passed in the parameters! Please use Amplituda constants", INVALID_FORMAT_FLAG_ERR);
    }
}
