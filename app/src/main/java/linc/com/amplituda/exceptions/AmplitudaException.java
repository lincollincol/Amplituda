package linc.com.amplituda.exceptions;

import static linc.com.amplituda.ErrorCode.AMPLITUDA_EXCEPTION;

public class AmplitudaException extends Exception {
    protected final int code;

    public AmplitudaException(final String message, final int code) {
        super(String.format("%s\nRead Amplituda doc here: https://github.com/lincollincol/Amplituda", message));
        this.code = code;
    }

    public AmplitudaException() {
        this("Something went wrong!", AMPLITUDA_EXCEPTION);
    }

    public int getCode() {
        return code;
    }
}