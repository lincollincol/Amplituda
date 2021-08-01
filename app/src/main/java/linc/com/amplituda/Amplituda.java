package linc.com.amplituda;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import linc.com.amplituda.callback.ErrorListener;
import linc.com.amplituda.callback.ListCallback;
import linc.com.amplituda.callback.StringCallback;
import linc.com.amplituda.exceptions.*;
import linc.com.amplituda.exceptions.io.*;
import linc.com.amplituda.exceptions.processing.*;

public final class Amplituda {

    public static final int SINGLE_LINE_SEQUENCE_FORMAT = 0;
    public static final int NEW_LINE_SEQUENCE_FORMAT = 1;

    public static final int SECONDS = 2;
    public static final int MILLIS = 3;

    private final ErrorListener errorListener;
    private final FileManager fileManager;
    private final List<AmplitudaException> errors = new LinkedList<>();

    private String amplitudes;

    private Amplituda(
            ErrorListener errorListener,
            FileManager fileManager,
            final int priority,
            final boolean enable
    ) {
        this.errorListener = errorListener;
        this.fileManager = fileManager;
        AmplitudaLogger.priority(priority);
        AmplitudaLogger.enable(enable);
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    public synchronized Amplituda fromFile(final File audio)  {
        // Clear previous data when Amplituda used repeatedly
        clearPreviousAmplitudaData();

        // Process audio
        if(!audio.exists()) {
            throwException(new FileNotFoundException());
        } else {
            if(!fileManager.isAudioFile(audio.getPath())) {
                throwException(new FileOpenException());
                return this;
            }
            // Save time before processing
            long start = System.currentTimeMillis();

            // Process input audio
            AmplitudaResultJNI result = amplitudesFromAudioJNI(audio.getPath());

            // Log processing time
            AmplitudaLogger.log(String.format(
                    Locale.getDefault(),
                    "Processing time: %.04f seconds",
                    ((System.currentTimeMillis() - start) / 1000f))
            );

            // Copy result data
            amplitudes = result.getAmplitudes();
            errors.addAll(result.getErrors());
            fileManager.stashPath(audio.getPath());
            // Emit all exceptions after subscribe
            handleAmplitudaErrors();
        }
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audio - path or url to input audio file
     */
    public Amplituda fromFile(final String audio) {
        if(URLUtil.isValidUrl(audio)) {
            if(!fileManager.cacheNotNull()) {
                throwException(new ExtendedProcessingDisabledException());
                return this;
            }
            File tempAudio = fileManager.getUrlFile(audio);
            if(tempAudio == null) {
                throwException(new InvalidAudioUrlException());
                return this;
            }
            fromFile(tempAudio);
            fileManager.deleteFile(tempAudio);
        } else {
            fromFile(new File(audio));
        }
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param rawId - path to source file
     */
    public Amplituda fromFile(int rawId) {
        if(!fileManager.cacheNotNull()) {
            throwException(new ExtendedProcessingDisabledException());
            return this;
        }
        File tempAudio = fileManager.getRawFile(rawId);
        if(tempAudio == null) {
            throwException(new InvalidRawResourceException());
            return this;
        }
        fromFile(tempAudio);
        fileManager.deleteFile(tempAudio);
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audio - path or url to input audio file
     * Please use `fromFile(final String audio)` instead of this method
     *
     * ONLY TO SUPPORT WaveformSeekBar library 3.0.0 version with new Ampituda versions
     * [https://github.com/massoudss/waveformSeekBar]
     */
    @Deprecated
    public Amplituda fromPath(final String audio) {
        fromFile(audio);
        return this;
    }

    /**
     * Convert result amplitudes to List
     * @param listCallback - result callback
     */
    public Amplituda amplitudesAsList(final ListCallback listCallback) {
        if(amplitudes == null || amplitudes.isEmpty())
            return this;

        String[] log = amplitudes.split("\n");
        List<Integer> amplitudes = new ArrayList<>();

        for (String amplitude : log) {
            if(amplitude.isEmpty()) {
                break;
            }
            amplitudes.add(Integer.valueOf(amplitude));
        }
        listCallback.call(amplitudes);
        return this;
    }

    /**
     * Convert result amplitudes to JSON format
     * @param jsonCallback - result callback
     */
    public Amplituda amplitudesAsJson(final StringCallback jsonCallback) {
        if(amplitudes == null || amplitudes.isEmpty())
            return this;
        jsonCallback.call("[" + amplitudesToSingleLineSequence(amplitudes, ", ") + "]");
        return this;
    }

    /**
     * Overload for amplitudesAsSequence method. Use space (" ") as a default delimiter
     * @param format - output format: single line or multiline output string
     * @param stringCallback - result callback
     */
    public Amplituda amplitudesAsSequence(final int format, final StringCallback stringCallback) {
        if(amplitudes == null || amplitudes.isEmpty())
            return this;

        amplitudesAsSequence(format, " ", stringCallback);
        return this;
    }

    /**
     * Convert result amplitudes to single line string with custom delimiter and send result to user via stringCallback
     * @param format - output format: single line or multiline output string
     * @param singleLineDelimiter - delimiter between amplitudes. WARNING: this parameter will be ignored when NEW_LINE_SEQUENCE_FORMAT passed as a parameter
     * @param stringCallback - result callback
     */
    public Amplituda amplitudesAsSequence(
            final int format,
            final String singleLineDelimiter,
            final StringCallback stringCallback
    ) {
        if(amplitudes == null || amplitudes.isEmpty())
            return this;

        switch (format) {
            case SINGLE_LINE_SEQUENCE_FORMAT: stringCallback.call(amplitudesToSingleLineSequence(
                    amplitudes,
                    singleLineDelimiter
            )); break;
            case NEW_LINE_SEQUENCE_FORMAT: stringCallback.call(amplitudes); break;
            default: throwException(new InvalidParameterFlagException()); break;
        }
        return this;
    }

    /**
     * Extracts list of amplitudes per specific second
     * @param second - specific second from input file
     * @param listCallback - result callback
     */
    public Amplituda amplitudesForSecond(final int second, final ListCallback listCallback) {
        amplitudesAsList(new ListCallback() {
            @Override
            public void call(List<Integer> data) {
                int duration = (int) getDuration(SECONDS);
                int aps = data.size() / duration; // amplitudes per second
                // Use second as a map key
                int currentSecond = 0;
                // Map with format = Map<Second, Amplitudes>
                Map<Integer, List<Integer>> amplitudes = new LinkedHashMap<>();
                // Temporary amplitudes list
                List<Integer> amplitudesPerSecond = new ArrayList<>();

                for(int sampleIndex = 0; sampleIndex < data.size(); sampleIndex++) {
                    if(sampleIndex % aps == 0) { // Save all amplitudes when current frame index equals to aps
                        // Save amplitudes to map
                        amplitudes.put(currentSecond, new ArrayList<>(amplitudesPerSecond));
                        // Clear temporary amplitudes
                        amplitudesPerSecond.clear();
                        // Increase current second
                        currentSecond++;
                    } else {
                        // Add amplitude to temporary list
                        amplitudesPerSecond.add(data.get(sampleIndex));
                    }
                }

                if(second > duration) {
                      throwException(new SecondOutOfBoundsException(second, duration));
                } else {
                    listCallback.call(amplitudes.get(second));
                }
            }
        });
        return this;
    }

    /**
     * Merge result amplitudes according to samplesPerSecond
     * @param samplesPerSecond - number of samples per audio second
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
    public Amplituda compressAmplitudes(final int samplesPerSecond) {
        amplitudesAsList(new ListCallback() {
            @Override
            public void call(List<Integer> data) {
                if(samplesPerSecond <= 0) {
                    throwException(new InvalidParameterFlagException());
                    return;
                }

                int duration = (int) getDuration(SECONDS);
                int aps = data.size() / duration;

                if(samplesPerSecond > aps) {
                    throwException(new SampleOutOfBoundsException(aps, samplesPerSecond));
                    return;
                }

                if(aps == samplesPerSecond) {
                    return;
                }

                int apsDivider = aps / samplesPerSecond;
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

                amplitudes = compressed.toString();
            }
        });
        return this;
    }

    /**
     * Returns duration from file in seconds or millis
     * @param format - output time format: SECONDS or MILLIS
     */
    public long getDuration(final int format) {
        String inputAudioFile = fileManager.getStashedPath();

        if(inputAudioFile == null) {
            throwException(new NoInputFileException());
            return 0;
        }

        if(format != SECONDS && format != MILLIS) {
            throwException(new InvalidParameterFlagException());
            return 0;
        }

        long duration = fileManager.getAudioDuration(inputAudioFile);

        if (format == SECONDS) {
            return duration / 1000;
        }
        return duration;
    }

    /**
     * Convert result amplitudes to single line string with delimiter
     * @param amplitudes - result from native c++ code
     * @param delimiter - amplitudes separator
     * @return string from amplitudes with custom delimiter. Example -> 0, 1, 2 | delimiter = ", "
     */
    private String amplitudesToSingleLineSequence(final String amplitudes, final String delimiter) {
        String[] log = amplitudes.split("\n");
        return TextUtils.join(delimiter, log);
    }

    /**
     * Emit new exception event for listener
     * @param exception - cause
     */
    private void throwException(final AmplitudaException exception) {
        if(errorListener == null) {
            errors.add(exception);
            return;
        }
        errorListener.call(exception);
    }

    /**
     * Handle errors from ndk side
     */
    private synchronized void handleAmplitudaErrors() {
        if(errors.isEmpty())
            return;
        for(final AmplitudaException exception : errors) {
            throwException(exception);
        }
        errors.clear();
    }

    /**
     * Clear local variables. Call this function when Amplituda object used repeatedly
     */
    private void clearPreviousAmplitudaData() {
        amplitudes = null;
        errors.clear();
        fileManager.clearStashedPath();
    }

    /**
     * NDK part
     */
    static {
        System.loadLibrary("native-lib");
    }

    native AmplitudaResultJNI amplitudesFromAudioJNI(String pathToAudio);

    public static final class Builder {

        private int logPriority = Log.DEBUG;
        private boolean logEnable = false;
        private ErrorListener errorListener = null;
        private final FileManager fileManager = new FileManager();

        /**
         * Observe and handle errors
         * @param errorListener - callback with exception as a parameter
         */
        public Builder setErrorListener(final ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }

        /**
         * Enable processing audio from url or res/raw
         * @param context - this param used for initialization
         *                cache directory and resources for res/raw
         */
        public Builder enableExtendedProcessing(final Context context) {
            fileManager.initCache(context);
            fileManager.initResources(context);
            return this;
        }

        /**
         * Enable Amplituda logs for mor processing information
         * @param priority - android Log constant. For example Log.DEBUG
         * @param enable - turn on / off logs
         */
        public Builder setLogConfig(final int priority, final boolean enable) {
            this.logPriority = priority;
            this.logEnable = enable;
            return this;
        }

        public Amplituda build() {
            return new Amplituda(
                    this.errorListener,
                    this.fileManager,
                    this.logPriority,
                    this.logEnable
            );
        }
    }

}
