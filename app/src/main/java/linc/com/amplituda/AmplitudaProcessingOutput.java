package linc.com.amplituda;

import java.util.LinkedHashSet;
import java.util.List;

import linc.com.amplituda.callback.AmplitudaErrorListener;
import linc.com.amplituda.callback.AmplitudaSuccessListener;
import linc.com.amplituda.exceptions.AmplitudaException;
import linc.com.amplituda.exceptions.processing.InvalidParameterFlagException;
import linc.com.amplituda.exceptions.processing.SampleOutOfBoundsException;

public final class AmplitudaProcessingOutput<T> {

    private final AmplitudaResult<T> amplitudaResult;
    private LinkedHashSet<AmplitudaException> processingErrors = new LinkedHashSet<>();

    AmplitudaProcessingOutput(
            final String amplitudes,
            final InputAudio<T> inputAudio
    ) {
        amplitudaResult = new AmplitudaResult<>(amplitudes, inputAudio);
    }

    AmplitudaProcessingOutput(
            final AmplitudaResultJNI processingData,
            final InputAudio<T> inputAudio
    ) {
        this(
            processingData.getAmplitudes(),
            inputAudio
        );
        this.processingErrors.addAll(processingData.getErrors());
    }

    AmplitudaProcessingOutput(final AmplitudaException exception, final InputAudio<T> inputAudio) {
        this("", inputAudio);
        this.processingErrors.add(exception);
    }

    /**
     * Get Amplituda processing result. This function returns result in callback
     * @param successListener - success processing operation callback
     * @param errorListener - processing error callback
     */
    public void get(
            final AmplitudaSuccessListener<T> successListener,
            final AmplitudaErrorListener errorListener
    ) {
        handleAmplitudaProcessingErrors(errorListener);
        successListener.onSuccess(amplitudaResult);
    }

    /**
     * Get Amplituda processing result. This function returns result in callback
     * @param successListener - success processing operation callback
     */
    public void get(final AmplitudaSuccessListener<T> successListener) {
        get(successListener, null);
    }

    /**
     * Get Amplituda processing result
     * @param errorListener - processing error callback
     * @return AmplitudaResult object
     */
    public AmplitudaResult<T> get(final AmplitudaErrorListener errorListener) {
        handleAmplitudaProcessingErrors(errorListener);
        return amplitudaResult;
    }

    /**
     * Get Amplituda processing result
     * @return AmplitudaResult object
     */
    public AmplitudaResult<T> get() {
        return amplitudaResult;
    }

    private void handleAmplitudaProcessingErrors(final AmplitudaErrorListener errorListener) {
        if(processingErrors.isEmpty()){
            processingErrors = null;
            return;
        }

        for(final AmplitudaException exception : processingErrors) {
            throwException(exception, errorListener);
        }

        processingErrors.clear();
        processingErrors = null;
    }

    private void throwException(
            final AmplitudaException exception,
            final AmplitudaErrorListener errorListener
    ) {
        if(errorListener == null) {
            processingErrors.add(exception);
            return;
        }
        errorListener.onError(exception);
    }

}
