package linc.com.amplituda;

import android.content.Context;
import android.webkit.URLUtil;

import java.io.File;
import java.io.InputStream;

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

    public AmplitudaProcessingOutput<InputStream> processAudio(final InputStream audio) {
        return processAudio(audio, null, null);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(final byte[] audio) {
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

    public AmplitudaProcessingOutput<InputStream> processAudio(final InputStream audio, final Compress compressParams) {
        return processAudio(audio, compressParams, null);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(final byte[] audio, final Compress compressParams) {
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

    public AmplitudaProcessingOutput<InputStream> processAudio(final InputStream audio, final AmplitudaProgressListener listener) {
        return processAudio(audio, null, listener);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(final byte[] audio, final AmplitudaProgressListener listener) {
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
                    getValidCompression(compress),
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
                        getValidCompression(compress),
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
                    getValidCompression(compress),
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
                    getValidCompression(compress),
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
     * @param audio - uri source file
     */
    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<InputStream> inputAudio = new InputAudio<>(audio, InputAudio.Type.INPUT_STREAM);
        try {
            updateProgressOperation(listener, ProgressOperation.DECODING);
            File audioFile = fileManager.getUriFile(audio, listener);
            AmplitudaProcessingOutput<InputStream> output = new AmplitudaProcessingOutput<>(
                    processFileJNI(
                            audioFile,
                            inputAudio,
                            getValidCompression(compress),
                            listener
                    ),
                    inputAudio
            );
            fileManager.deleteFile(audioFile);
            return output;
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - uri source file
     */
    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<byte[]> inputAudio = new InputAudio<>(audio, InputAudio.Type.BYTE_ARRAY);
        try {
            updateProgressOperation(listener, ProgressOperation.DECODING);
            File audioFile = fileManager.getByteArrayFile(audio, listener);
            AmplitudaProcessingOutput<byte[]> output = new AmplitudaProcessingOutput<>(
                    processFileJNI(
                        audioFile,
                        inputAudio,
                        getValidCompression(compress),
                        listener
                    ),
                    inputAudio
            );
            fileManager.deleteFile(audioFile);
            return output;
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    private <T> AmplitudaResultJNI processFileJNI(
            final File audio,
            final InputAudio<T> inputAudio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) throws AmplitudaException {
        // Process audio
        if(!audio.exists()) {
            throw new FileNotFoundException();
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

    private Compress getValidCompression(Compress compress) {
        if(compress == null || compress.isNotValid()) {
            return Compress.withParams(Compress.NONE, Compress.NONE);
        }
        return compress;
    }

    private synchronized <T> AmplitudaProcessingOutput<T> errorOutput(
            final AmplitudaException exception,
            final InputAudio<T> inputAudio,
            final AmplitudaProgressListener listener
    ) {
        stopProgress(listener);
        return new AmplitudaProcessingOutput<T>(exception, inputAudio);
    }

    private synchronized void stopProgress(final AmplitudaProgressListener listener) {
        if(listener != null) {
            listener.onStopProgress();
        }
    }

    private synchronized void startProgress(final AmplitudaProgressListener listener) {
        if(listener != null) {
            listener.onStartProgress();
        }
    }

    private synchronized void updateProgressOperation(
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