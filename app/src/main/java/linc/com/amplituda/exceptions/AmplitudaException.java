package linc.com.amplituda.exceptions;

public class AmplitudaException extends Exception {
    protected final int code;

    public AmplitudaException(final String message, final int code) {
        super(String.format("%s\nRead Amplituda doc here: https://github.com/lincollincol/Amplituda", message));
        this.code = code;
    }

    /** @hide */ public int getCode() {
        return code;
    }
}