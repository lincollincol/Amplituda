package com.linc.amplituda.exceptions.processing;

import java.util.Locale;

import static com.linc.amplituda.ErrorCode.SAMPLE_OUT_OF_BOUNDS_PROC_CODE;

public final class SampleOutOfBoundsException extends AmplitudaProcessingException {

    public SampleOutOfBoundsException() {
        super("Sample out of bound. Check `Compress` parameter: preferredSamplesPerSecond.",
                SAMPLE_OUT_OF_BOUNDS_PROC_CODE
        );
    }
}
