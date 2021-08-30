package linc.com.amplituda;

import android.content.Context;
import android.webkit.URLUtil;

import java.io.File;

import linc.com.amplituda.exceptions.*;
import linc.com.amplituda.exceptions.io.*;

public final class Amplituda {

    private final FileManager fileManager;

    public Amplituda(final Context context) {
        fileManager = new FileManager(context);
    }

    /**
     * Enable Amplituda logs for mor processing information
     * @param priority - android Log constant. For example Log.DEBUG
     * @param enable - turn on / off logs
     */
    public Amplituda setLogConfig(final int priority, final boolean enable) {
        AmplitudaLogger.enable(enable);
        AmplitudaLogger.priority(priority);
        return this;
    }

    /** Audio file only */

    public AmplitudaProcessingOutput<File> processAudio(final File audio) {
        return processAudio(audio, null, null);
    }

    public AmplitudaProcessingOutput<String> processAudio(final String audio) {
        return processAudio(audio, null, null);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(final int audio) {
        return processAudio(audio, null, null);
    }

    /** Audio file + compress params */

    public AmplitudaProcessingOutput<File> processAudio(final File audio, final Compress compressParams) {
        return processAudio(audio, compressParams, null);
    }

    public AmplitudaProcessingOutput<String> processAudio(final String audio, final Compress compressParams) {
        return processAudio(audio, compressParams, null);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(final int audio, final Compress compressParams) {
        return processAudio(audio, compressParams, null);
    }

    /** Audio file + progress listener */

    public AmplitudaProcessingOutput<File> processAudio(final File audio, final AmplitudaProgressListener listener) {
        return processAudio(audio, null, listener);
    }

    public AmplitudaProcessingOutput<String> processAudio(final String audio, final AmplitudaProgressListener listener) {
        return processAudio(audio, null, listener);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(final int audio, final AmplitudaProgressListener listener) {
        return processAudio(audio, null, listener);
    }

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<File> inputAudio = new InputAudio<>(audio, InputAudio.Type.FILE);
        try {
            return new AmplitudaProcessingOutput<>(processFileJNI(
                    audio,
                    inputAudio,
                    compress == null ? Compress.withParams(Compress.NONE, Compress.NONE) : compress,
                    listener
            ), inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - local path or url to input audio file
     */
    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<String> inputAudio = new InputAudio<>(audio);
        try {
            // When audio is local file - process as file
            if(!URLUtil.isValidUrl(audio)) {
                inputAudio.setType(InputAudio.Type.PATH);
                return new AmplitudaProcessingOutput<>(processFileJNI(
                        new File(audio),
                        inputAudio,
                        compress == null ? Compress.withParams(Compress.NONE, Compress.NONE) : compress,
                        listener
                ), inputAudio);
            }
            // When audio is URL
            inputAudio.setType(InputAudio.Type.URL);

            // Save start time
            long startTime = System.currentTimeMillis();

            updateProgressOperation(listener, ProgressOperation.DOWNLOADING);

            // Copy audio from url to local storage
            File tempAudio = fileManager.getUrlFile(audio, listener);

            // Check for success copy operation from url to local tmp
            if(tempAudio == null) {
                return errorOutput(new InvalidAudioUrlException(), inputAudio, listener);
            }

            // Log operation time
            AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PREPARING, startTime);

            // Process local audio
            AmplitudaResultJNI result = processFileJNI(
                    tempAudio,
                    inputAudio,
                    compress == null ? Compress.withParams(Compress.NONE, Compress.NONE) : compress,
                    listener
            );

            // Remove tmp file
            fileManager.deleteFile(tempAudio);

            return new AmplitudaProcessingOutput<>(result, inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - path to res/raw source file
     */
    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<Integer> inputAudio = new InputAudio<>(audio, InputAudio.Type.RESOURCE);

        // Save start time
        long startTime = System.currentTimeMillis();

        updateProgressOperation(listener, ProgressOperation.DECODING);

        // Copy raw to local file
        File tempAudio = fileManager.getRawFile(audio, listener);

        // Check for success copy operation from res to local tmp
        if(tempAudio == null) {
            return errorOutput(new InvalidRawResourceException(), inputAudio, listener);
        }

        // Log operation time
        AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PREPARING, startTime);

        try {
            // Process local raw file
            AmplitudaResultJNI result = processFileJNI(
                    tempAudio,
                    inputAudio,
                    compress == null ? Compress.withParams(Compress.NONE, Compress.NONE) : compress,
                    listener
            );

            // Delete tmp
            fileManager.deleteFile(tempAudio);

            return new AmplitudaProcessingOutput<>(result, inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    private synchronized <T> AmplitudaResultJNI processFileJNI(
            final File audio,
            final InputAudio<T> inputAudio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) throws AmplitudaException {
        // Process audio
        if(!audio.exists()) {
            throw new FileNotFoundException();
        }

        if(!fileManager.isAudioFile(audio.getPath())) {
            throw new FileOpenException();
        }

        // Save start time
        long startTime = System.currentTimeMillis();

        updateProgressOperation(listener, ProgressOperation.PROCESSING);

        // Process input audio
        AmplitudaResultJNI result = amplitudesFromAudioJNI(
                audio.getPath(),
                compress.getType(),
                compress.getPreferredSamplesPerSecond(),
                listener
        );

        // Save audio duration when file is valid and was processed
        inputAudio.setDuration(result.getDurationMillis());

        // Log operation time
        AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PROCESSING, startTime);

        // Stop progress after processing
        stopProgress(listener);
        return result;
    }

    private <T> AmplitudaProcessingOutput<T> errorOutput(
            final AmplitudaException exception,
            final InputAudio<T> inputAudio,
            final AmplitudaProgressListener listener
    ) {
        stopProgress(listener);
        return new AmplitudaProcessingOutput<T>(exception, inputAudio);
    }

    private void stopProgress(final AmplitudaProgressListener listener) {
        if(listener != null) {
            listener.onStopProgress();
        }
    }

    private void startProgress(final AmplitudaProgressListener listener) {
        if(listener != null) {
            listener.onStartProgress();
        }
    }

    private void updateProgressOperation(
            final AmplitudaProgressListener listener,
            final ProgressOperation operation
    ) {
        if(listener != null) {
            listener.onOperationChanged(operation);
        }
    }

    /**
     * NDK part
     */
    static {
        System.loadLibrary("native-lib");
    }

    native AmplitudaResultJNI amplitudesFromAudioJNI(
            String pathToAudio,
            int compressType,
            int framesPerSecond,
            AmplitudaProgressListener listener
    );

}
