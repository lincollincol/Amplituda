package linc.com.amplituda.exceptions.processing;

import java.util.Locale;

import linc.com.amplituda.exceptions.AmplitudaException;

import static linc.com.amplituda.ErrorCode.SECOND_OUT_OF_BOUNDS_ERR;

public class SecondOutOfBoundsException extends AmplitudaProcessingException {
    public SecondOutOfBoundsException(int second, int duration) {
        super(String.format(
                Locale.getDefault(),
                "Cannot extract amplitudes for second %d when input audio duration = %d", second, duration),
                SECOND_OUT_OF_BOUNDS_ERR
        );
    }
}
