package linc.com.amplituda;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatFlagsException;

import linc.com.amplituda.exceptions.*;
import linc.com.amplituda.exceptions.io.*;
import linc.com.amplituda.exceptions.allocation.*;

import static linc.com.amplituda.ErrorCode.*;

public final class Amplituda {

    private static final String LIB_TAG = "AMPLITUDA";

    public static final int SINGLE_LINE_SEQUENCE_FORMAT = 0;
    public static final int NEW_LINE_SEQUENCE_FORMAT = 1;

    public static final int SECONDS = 2;
    public static final int MILLIS = 3;

    private String amplitudes;
    private ErrorListener errorListener;

    private final FileManager fileManager;
    private final RawExtractor rawExtractor;

    public Amplituda(Context context) {
        fileManager = new FileManager(context);
        rawExtractor = new RawExtractor(context, fileManager);
    }

    public Amplituda setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    public synchronized Amplituda fromFile(final File audio)  {
        if(!audio.exists()) {
            throwException(new FileNotFoundException());
        } else {
            fileManager.stashPath(audio.getPath());
            AmplitudaResultJNI result = amplitudesFromAudioJNI(audio.getPath());
            handleErrors(result.getErrors());
            amplitudes = result.getAmplitudes();
        }
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audioPath - path to source file
     */
    public Amplituda fromPath(final String audioPath) {
        fromFile(new File(audioPath));
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param rawId - path to source file
     */
    public Amplituda fromFile(int rawId) {
        File audio = rawExtractor.getAudioFromRawResources(rawId);
        if(audio == null) {
            throwException(new InvalidRawResourceException());
            return this;
        }
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
        listCallback.onEvent(amplitudes);
        return this;
    }

    /**
     * Convert result amplitudes to JSON format
     * @param jsonCallback - result callback
     */
    public Amplituda amplitudesAsJson(final StringCallback jsonCallback) {
        if(amplitudes == null)
            return this;
        jsonCallback.onEvent("[" + amplitudesToSingleLineSequence(amplitudes, ", ") + "]");
        return this;
    }

    /**
     * Overload for amplitudesAsSequence method. Use space (" ") as a default delimiter
     * @param format - output format: single line or multiline output string
     * @param stringCallback - result callback
     */
    public Amplituda amplitudesAsSequence(final int format, final StringCallback stringCallback) {
        if(amplitudes == null)
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
        if(amplitudes == null)
            return this;
        switch (format) {
            case SINGLE_LINE_SEQUENCE_FORMAT: stringCallback.onEvent(amplitudesToSingleLineSequence(
                    amplitudes,
                    singleLineDelimiter
            )); break;
            case NEW_LINE_SEQUENCE_FORMAT: stringCallback.onEvent(amplitudes); break;
            default: throwException(new InvalidFormatFlagException()); break;
        }
        return this;
    }

    /**
     * Extracts list of amplitudes per specific second
     * @param second - specific second from input file
     * @param listCallback - result callback
     */
    public void amplitudesPerSecond(final int second, final ListCallback listCallback) {
        amplitudesAsList(new ListCallback() {
            @Override
            public void onEvent(List<Integer> list) {
                int duration = (int) getDuration(SECONDS);
                int aps = list.size() / duration; // amplitudes per second
                // Use second as a map key
                int currentSecond = 0;
                // Map with format = Map<Second, Amplitudes>
                Map<Integer, List<Integer>> amplitudes = new LinkedHashMap<>();
                // Temporary amplitudes list
                List<Integer> amplitudesPerSecond = new ArrayList<>();

                for(int frameIndex = 0; frameIndex < list.size(); frameIndex++) {
                    if(frameIndex % aps == 0) { // Save all amplitudes when current frame index equals to aps
                        // Save amplitudes to map
                        amplitudes.put(currentSecond, new ArrayList<>(amplitudesPerSecond));
                        // Clear temporary amplitudes
                        amplitudesPerSecond.clear();
                        // Increase current second
                        currentSecond++;
                    } else {
                        // Add amplitude to temporary list
                        amplitudesPerSecond.add(list.get(frameIndex));
                    }
                }

                if(second > duration) {
                      throwException(new SecondOutOfBoundsException(second, duration));
                } else {
                    listCallback.onEvent(amplitudes.get(second));
                }
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
            throwException(new InvalidFormatFlagException());
            return 0;
        }

        long duration = Long.parseLong(getDurationStr(inputAudioFile));

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
     * Extracts duration from input audio file
     * @return duration in String format
     */
    private String getDurationStr(final String path) throws IllegalArgumentException {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    }

    /**
     * Emit new exception event for listener
     * @param exception - cause
     */
    private void throwException(final AmplitudaException exception) {
        if(errorListener == null) {
            return;
        }
        errorListener.onEvent(exception);
    }

    /**
     * Handle and emit exc
     * @param exception - cause
     */
    private void handleErrors(final Collection<Integer> errors) {
        for(final int code : errors) {
            switch (code) {
                case ALLOC_FRAME_ERR:        throwException(new FrameAllocationException());           break;
                case ALLOC_PACKET_ERR:       throwException(new PacketAllocationException());          break;
                case ALLOC_CODEC_CTX_ERR:    throwException(new CodecContextAllocationException());    break;
                case NOT_FOUND_CODEC:        throwException(new CodecNotFoundException());             break;
                case NOT_FOUND_STREAM:       throwException(new StreamNotFoundException());            break;
                case NOT_FOUND_STREAM_INFO:  throwException(new StreamInformationNotFoundException()); break;
                case CODEC_PARAMETERS_ERR:   throwException(new CodecParametersException());           break;
                case PACKET_SUBMITTING_ERR:  throwException(new PacketSubmittingException());          break;
                case FILE_OPEN_ERR:          throwException(new FileOpenException());                  break;
                case CODEC_OPEN_ERR:         throwException(new CodecOpenException());                 break;
                case UNSUPPORTED_SAMPLE_FMT: throwException(new UnsupportedSampleFormatException());   break;
                case DECODING_ERR:           throwException(new DecodingException());                  break;
                default: break;
            }
        }
    }

    /**
     * Base Callback interface
     */
    private interface AmplitudaCallback<T> { void onEvent(T amplitudesResult); }

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
