package linc.com.amplituda.exceptions;

public class InvalidFormatFlagException extends AmplitudaException {
    public InvalidFormatFlagException() {
        super("Invalid format passed in the parameters! Please use Amplituda constants.");
    }
}
