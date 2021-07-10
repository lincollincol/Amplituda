package linc.com.amplituda.exceptions.processing;

import java.util.Locale;

import static linc.com.amplituda.ErrorCode.SAMPLE_OUT_OF_BOUNDS_PROC_CODE;

public class SampleOutOfBoundsException extends AmplitudaProcessingException {

    public SampleOutOfBoundsException(int aps, int sps) {
        super(String.format(
                Locale.getDefault(),
                "Sample out of bound. Current max samples per second: %d. Preferred samples per second: %d", aps, sps),
                SAMPLE_OUT_OF_BOUNDS_PROC_CODE
        );
    }

    public SampleOutOfBoundsException() {
        this(0, 0);
    }

}
