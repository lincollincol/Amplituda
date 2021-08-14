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

    private AmplitudaProcessingOutput(
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
     * Merge result amplitudes according to samplesPerSecond
     * @param preferredSamplesPerSecond - number of samples per audio second
     * For example:
     *     audio duration = 200 seconds
     *     after Amplituda processing, 1 second contains 40 samples
     *     200 seconds contains 200 * 40 = 8000
     *     case 1: samplesPerSecond = 1, function will merge this 40 samples to 1.
     *                         Output size will be 200 amplitudes
     *     case 2: samplesPerSecond = 20, function will merge this 40 samples to 20.
     *                         Output size will be 4000 amplitudes
     * Advantage: small output size
     * Disadvantage: output quality
     */
    public AmplitudaProcessingOutput<T> compress(final int preferredSamplesPerSecond) {
        List<Integer> data = amplitudaResult.amplitudesAsList();

        if(preferredSamplesPerSecond <= 0) {
            throwException(new InvalidParameterFlagException(), null);
            return this;
        }

        int duration = (int) amplitudaResult.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS);
        int aps = data.size() / duration;

        if(preferredSamplesPerSecond > aps) {
            throwException(new SampleOutOfBoundsException(aps, preferredSamplesPerSecond), null);
            return this;
        }

        if(aps == preferredSamplesPerSecond) {
            return this;
        }

        int apsDivider = aps / preferredSamplesPerSecond;
        int sum = 0;
        StringBuilder compressed = new StringBuilder();

        if(apsDivider < 2) {
            apsDivider = 2;
        }

        for(int sampleIndex = 0; sampleIndex < data.size(); sampleIndex++) {
            if(sampleIndex % apsDivider == 0) {
                compressed.append(sum / apsDivider);
                compressed.append('\n');
                sum = 0;
            } else {
                sum += data.get(sampleIndex);
            }
        }

        amplitudaResult.setAmplitudes(compressed.toString());
        return this;
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
