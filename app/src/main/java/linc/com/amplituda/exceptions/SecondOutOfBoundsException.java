package linc.com.amplituda.exceptions;

import java.util.Locale;

public class SecondOutOfBoundsException extends AmplitudaException {
    public SecondOutOfBoundsException(int second, int duration) {
        super(String.format(Locale.getDefault(), "Cannot extract amplitudes for second %d when input audio duration = %d", second, duration));
    }
}
