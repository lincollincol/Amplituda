package linc.com.amplituda;

import android.content.Context;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

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

    /**
     * Clear cache by audio and key or completely
     */
    public Amplituda clearCache() {
        fileManager.clearAllCacheFiles();
        return this;
    }

    public Amplituda clearCache(final File audio) {
        clearCache(String.valueOf(audio.hashCode()), true);
        return this;
    }

    public Amplituda clearCache(final String audio, final boolean isKey) {
        fileManager.clearCache(isKey ? audio : String.valueOf(audio.hashCode()));
        return this;
    }

    public Amplituda clearCache(final int audio) {
        clearCache(String.valueOf(audio), true);
        return this;
    }

    public Amplituda clearCache(final InputStream audio) {
        clearCache(fileManager.getInputStreamHashString(audio), true);
        return this;
    }

    public Amplituda clearCache(final byte[] audio) {
        clearCache(String.valueOf(Arrays.hashCode(audio)), true);
        return this;
    }

    /** Audio file only */

    public AmplitudaProcessingOutput<File> processAudio(final File audio) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<String> processAudio(final String audio) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(final int audio) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(final InputStream audio) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(final byte[] audio) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), null);
    }

    /** Audio file + compress params */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Compress compressParams
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Compress compressParams
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Compress compressParams
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Compress compressParams
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), null);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Compress compressParams
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), null);
    }

    /** Audio file + cache params */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Cache cacheParams
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, null);
    }

    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Cache cacheParams
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, null);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Cache cacheParams
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, null);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Cache cacheParams
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, null);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Cache cacheParams
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, null);
    }

    /** Audio file + compress params + cache params */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Compress compress,
            final Cache cacheParams
    ) {
        return processAudio(audio, compress, cacheParams, null);
    }

    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Compress compress,
            final Cache cacheParams
    ) {
        return processAudio(audio, compress, cacheParams, null);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Compress compress,
            final Cache cacheParams
    ) {
        return processAudio(audio, compress, cacheParams, null);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Compress compress,
            final Cache cacheParams
    ) {
        return processAudio(audio, compress, cacheParams, null);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Compress compress,
            final Cache cacheParams
    ) {
        return processAudio(audio, compress, cacheParams, null);
    }

    /** Audio file + progress listener */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), Cache.getDefault(), listener);
    }

    /** Audio file + compress params + progress listener */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Compress compressParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Compress compressParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Compress compressParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Compress compressParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), listener);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Compress compressParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, compressParams, Cache.getDefault(), listener);
    }

    /** Audio file + cache params + progress listener */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Cache cacheParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, listener);
    }

    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Cache cacheParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, listener);
    }

    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Cache cacheParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, listener);
    }

    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Cache cacheParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, listener);
    }

    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Cache cacheParams,
            final AmplitudaProgressListener listener
    ) {
        return processAudio(audio, Compress.getDefault(), cacheParams, listener);
    }

    /** Audio file + compress params + cache params + progress listener */

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Compress compress,
            final Cache cache,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<File> inputAudio = new InputAudio<>(audio, InputAudio.Type.FILE);
        try {
            return new AmplitudaProcessingOutput<>(
                    processFileJNI(audio, inputAudio, compress, cache, listener),
                    inputAudio
            );
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
            final Cache cache,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<String> inputAudio = new InputAudio<>(audio);
        try {
            // When audio is local file - process as file
            if(!URLUtil.isValidUrl(audio)) {
                inputAudio.setType(InputAudio.Type.PATH);
                return new AmplitudaProcessingOutput<>(
                        processFileJNI(new File(audio), inputAudio, compress, cache, listener),
                        inputAudio
                );
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
            AmplitudaResultJNI result =
                    processFileJNI(tempAudio, inputAudio, compress, cache, listener);

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
            final Cache cache,
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
            AmplitudaResultJNI result = processFileJNI(tempAudio, inputAudio, compress, cache, listener);
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
     * @param audio - input stream audio source file (uri content input stream)
     */
    public AmplitudaProcessingOutput<InputStream> processAudio(
            final InputStream audio,
            final Compress compress,
            final Cache cache,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<InputStream> inputAudio = new InputAudio<>(audio, InputAudio.Type.INPUT_STREAM);
        try {
            updateProgressOperation(listener, ProgressOperation.DECODING);
            // Copy input stream to local file
            File tempAudio = fileManager.getInputStreamFile(audio, listener);
            // Check for success copy operation from IS to local tmp
            if(tempAudio == null) {
                return errorOutput(new InvalidAudioInputStreamException(), inputAudio, listener);
            }
            AmplitudaProcessingOutput<InputStream> output = new AmplitudaProcessingOutput<>(
                    processFileJNI(tempAudio, inputAudio, compress, cache, listener),
                    inputAudio
            );
            fileManager.deleteFile(tempAudio);
            return output;
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - byte array source file
     */
    public AmplitudaProcessingOutput<byte[]> processAudio(
            final byte[] audio,
            final Compress compress,
            final Cache cache,
            final AmplitudaProgressListener listener
    ) {
        startProgress(listener);
        InputAudio<byte[]> inputAudio = new InputAudio<>(audio, InputAudio.Type.BYTE_ARRAY);
        try {
            updateProgressOperation(listener, ProgressOperation.DECODING);
            // Copy raw to local file
            File tempAudio = fileManager.getByteArrayFile(audio, listener);
            // Check for success copy operation from IS to local tmp
            if(tempAudio == null) {
                return errorOutput(new InvalidAudioByteArrayException(), inputAudio, listener);
            }
            AmplitudaProcessingOutput<byte[]> output = new AmplitudaProcessingOutput<>(
                    processFileJNI(tempAudio, inputAudio, compress, cache, listener),
                    inputAudio
            );
            fileManager.deleteFile(tempAudio);
            return output;
        } catch (AmplitudaException exception) {
            // Handle processing error
            return errorOutput(exception, inputAudio, listener);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audioFile - source file
     */
    private synchronized <T> AmplitudaResultJNI processFileJNI(
            final File audioFile,
            final InputAudio<T> inputAudio,
            final Compress compress,
            final Cache cache,
            final AmplitudaProgressListener listener
    ) throws AmplitudaException {
        // Process audio
        if(!audioFile.exists()) {
            throw new FileNotFoundException();
        }
        // Save start time
        long startTime = System.currentTimeMillis();
        updateProgressOperation(listener, ProgressOperation.PROCESSING);
        File cacheFile = fileManager.getCacheFile(
                String.valueOf(audioFile.hashCode()),
                cache.getKey()
        );
        AmplitudaResultJNI result = null;
        if(cache.getState() == Cache.REUSE) {
            result = amplitudesFromCache(cacheFile);
        }
        if(result == null) {
            AmplitudaLogger.log("Process audio " + audioFile.getPath());
            // Process input audio
            result = amplitudesFromAudioJNI(
                    audioFile.getPath(),
                    compress.getType(),
                    compress.getPreferredSamplesPerSecond(),
                    cacheFile.getPath(),
                    cache.isEnabled(),
                    listener
            );
        } else {
            AmplitudaLogger.log(
                    String.format(
                            Locale.US,
                            "Found cache data \"%s\" for audio \"%s\"",
                            cacheFile.getName(),
                            audioFile.getName()
                    )
            );
        }
        // Save audio duration when file is valid and was processed
        inputAudio.setDuration(result.getDurationMillis());
        // Log operation time
        AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PROCESSING, startTime);
        // Stop progress after processing
        stopProgress(listener);
        return result;
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

    private AmplitudaResultJNI amplitudesFromCache(
            final File audioCache
    ) {
        try {
            String cacheData = fileManager.readFile(audioCache);
            if(cacheData == null || cacheData.isEmpty()) {
                return null;
            }
            int durationStartIdx = cacheData.indexOf("=");
            int durationEndIdx = cacheData.indexOf(System.lineSeparator());
            String duration = cacheData.substring(0, durationEndIdx)
                    .substring(durationStartIdx + 1, durationEndIdx);
            String amplitudes = cacheData.substring(durationEndIdx + 1);
            AmplitudaResultJNI resultJNI = new AmplitudaResultJNI();
            resultJNI.setDuration(Double.parseDouble(duration));
            resultJNI.setAmplitudes(amplitudes);
            return resultJNI;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * NDK part
     */
    static {
        System.loadLibrary("amplituda-native-lib");
    }

    native AmplitudaResultJNI amplitudesFromAudioJNI(
            String pathToAudio,
            int compressType,
            int framesPerSecond,
            String pathToCache,
            boolean cacheEnabled,
            AmplitudaProgressListener listener
    );

}