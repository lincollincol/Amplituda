package linc.com.amplituda;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import linc.com.amplituda.exceptions.*;
import linc.com.amplituda.exceptions.io.*;
import linc.com.amplituda.exceptions.allocation.*;
import linc.com.amplituda.exceptions.processing.*;

import static linc.com.amplituda.ErrorCode.*;

public final class Amplituda {

    public static final int SINGLE_LINE_SEQUENCE_FORMAT = 0;
    public static final int NEW_LINE_SEQUENCE_FORMAT = 1;

    public static final int SECONDS = 2;
    public static final int MILLIS = 3;

    private ErrorListener errorListener;

    private String amplitudes;
    private final List<Integer> errors = new LinkedList<>();

    private final FileManager fileManager;
    private final RawExtractor rawExtractor;

    public Amplituda(Context context) {
        fileManager = new FileManager(context);
        rawExtractor = new RawExtractor(context, fileManager);
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
        }
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audioPath - path to source file
     */
    public Amplituda fromFile(final String audioPath) {
        fromFile(new File(audioPath));
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param rawId - path to source file
     */
    public Amplituda fromFile(int rawId) {
        File tempAudio = rawExtractor.getAudioFromRawResources(rawId);
        if(tempAudio == null) {
            throwException(new InvalidRawResourceException());
            return this;
        }
        fromFile(tempAudio);
        fileManager.deleteFile(tempAudio);
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
     * Observe and handle errors
     * @param errorListener - callback with exception as a parameter
     */
    public Amplituda setErrorListener(final ErrorListener errorListener) {
        this.errorListener = errorListener;
        // Emit all exceptions after subscribe
        handleAmplitudaErrors();
        return this;
    }

    /**
     * Enable Amplituda logs for mor processing information
     * @param priority - android Log constant. For example Log.DEBUG
     * @param enable - turn on / off logs
     */
    public Amplituda setLogConfig(final int priority, final boolean enable) {
        AmplitudaLogger.priority(priority);
        AmplitudaLogger.enable(enable);
        return this;
    }

    /**
     * Extracts list of amplitudes per specific second
     * @param second - specific second from input file
     * @param listCallback - result callback
     */
    public void amplitudesForSecond(final int second, final ListCallback listCallback) {
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
    }

    public void compressAmplitudes(final int samplesPerSecond) {
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
            errors.add(exception.getCode());
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
        for(final int code : errors) {
            throwException(getExceptionFromCode(code));
        }
        errors.clear();
    }

    /**
     * Get exception according to code
     * @param code - exception code. All codes => ErrorCode.java
     * @return exception from code. Return global AmplitudaException when code is unknown
     */

    private AmplitudaException getExceptionFromCode(final int code) {
        switch (code) {
            case FRAME_ALLOC_CODE:                 return new FrameAllocationException();
            case PACKET_ALLOC_CODE:                return new PacketAllocationException();
            case CODEC_CONTEXT_ALLOC_CODE:         return new CodecContextAllocationException();
            case FILE_OPEN_IO_CODE:                return new FileOpenException();
            case FILE_NOT_FOUND_IO_CODE:           return new FileNotFoundException();
            case INVALID_RAW_RESOURCE_IO_CODE:     return new InvalidRawResourceException();
            case NO_INPUT_FILE_IO_CODE:            return new NoInputFileException();
            case CODEC_NOT_FOUND_PROC_CODE:        return new CodecNotFoundException();
            case STREAM_NOT_FOUND_PROC_CODE:       return new StreamNotFoundException();
            case STREAM_INFO_NOT_FOUND_PROC_CODE:  return new StreamInformationNotFoundException();
            case CODEC_PARAMETERS_COPY_PROC_CODE:  return new CodecParametersException();
            case PACKET_SUBMITTING_PROC_CODE:      return new PacketSubmittingException();
            case CODEC_OPEN_PROC_CODE:             return new CodecOpenException();
            case UNSUPPORTED_SAMPLE_FMT_PROC_CODE: return new UnsupportedSampleFormatException();
            case DECODING_PROC_CODE:               return new DecodingException();
            case INVALID_PARAMETER_FLAG_PROC_CODE:    return new InvalidParameterFlagException();
            case SECOND_OUT_OF_BOUNDS_PROC_CODE:   return new SecondOutOfBoundsException();
            default:                               return new AmplitudaException();
        }
    }

    /**
     * Clear local variables. Call this function when Amplituda object used repeatedly
     */
    private void clearPreviousAmplitudaData() {
        amplitudes = null;
        errors.clear();
        if(fileManager != null) {
            fileManager.clearStashedPath();
        }
    }

    /**
     * Base Callback interface
     */
    private interface AmplitudaCallback<T> { void call(T data); }

    /**
     * Callback interface for list output
     */
    public interface ListCallback extends AmplitudaCallback<List<Integer>> {}

    /**
     * Callback interface for string output
     */
    public interface StringCallback extends AmplitudaCallback<String> {}

    /**
     * Callback interface for error events
     */
    public interface ErrorListener extends AmplitudaCallback<AmplitudaException> {}

    /**
     * NDK part
     */
    static {
        System.loadLibrary("native-lib");
    }

    native AmplitudaResultJNI amplitudesFromAudioJNI(String pathToAudio);

}
