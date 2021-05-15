package linc.com.amplituda;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UnknownFormatFlagsException;

import static linc.com.amplituda.FileManager.AUDIO_TEMP;
import static linc.com.amplituda.FileManager.TXT_TEMP;

public final class Amplituda {

    public static final int SINGLE_LINE_SEQUENCE_FORMAT = 0;
    public static final int NEW_LINE_SEQUENCE_FORMAT = 1;

    public static final int SECONDS = 2;
    public static final int MILLIS = 3;

    private static final String APP_TAG = "AMPLITUDA";

    private String amplitudes;

    public Amplituda(Context context) {
        FileManager.init(context);
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    public synchronized Amplituda fromFile(final File audio)  {
        if(!audio.exists()) {
            Log.e(APP_TAG, "Wrong file! Please check path and try again!");
        } else {
            FileManager.clearCache();
            FileManager.saveRuntimePath(audio.getPath());
            int code = amplitudesFromAudioJNI(
                    audio.getPath(),
                    FileManager.provideTempFile(TXT_TEMP),
                    FileManager.provideTempFile(AUDIO_TEMP)
            );
            if(code != 0) {
                Log.e(APP_TAG, "Something went wrong! Check error log with \"Amplituda\" tag!");
            }
            this.amplitudes = FileManager.readFile(FileManager.provideTempFile(TXT_TEMP));
            FileManager.clearCache();
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
        listCallback.onSuccess(amplitudes);
        return this;
    }

    /**
     * Convert result amplitudes to JSON format
     * @param jsonCallback - result callback
     */
    public Amplituda amplitudesAsJson(final StringCallback jsonCallback) {
        if(amplitudes == null)
            return this;
        jsonCallback.onSuccess("[" + amplitudesToSingleLineSequence(amplitudes, ", ") + "]");
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
            case 0: {
                stringCallback.onSuccess(amplitudesToSingleLineSequence(amplitudes, singleLineDelimiter));
                break;
            }
            case 1: {
                stringCallback.onSuccess(amplitudes);
                break;
            }
            default: throw new UnknownFormatFlagsException("Use SINGLE_LINE_SEQUENCE_FORMAT or NEW_LINE_SEQUENCE_FORMAT as a parameter when you call amplitudesAsSequence!");
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
            public void onSuccess(List<Integer> list) {
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
                    Log.e(APP_TAG, String.format("Cannot extract amplitudes for second %d when input audio duration = %d", second, duration));
                    listCallback.onSuccess(new ArrayList<Integer>());
                } else {
                    listCallback.onSuccess(amplitudes.get(second));
                }
            }
        });
    }

    /**
     * Returns duration from file in seconds or millis
     * @param type - output time format: SECONDS or MILLIS
     */
    public long getDuration(final int type) {
        long duration = Long.parseLong(getDurationStr());
        if (type == SECONDS) {
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
    private String getDurationStr() {
        String inputAudioFile = FileManager.retrieveRuntimePath();
        if(inputAudioFile == null) {
            Log.e(APP_TAG, "Input file not found! Please call fromFile() or fromPath() at first!");
            return "0";
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(inputAudioFile);
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    }

    /**
     * Base Callback interface
     */
    private interface AmplitudaCallback<T> { void onSuccess(T amplitudesResult);}

    /**
     * Callback interface for list output
     */
    public interface ListCallback extends AmplitudaCallback<List<Integer>> {}

    /**
     * Callback interface for string output
     */
    public interface StringCallback extends AmplitudaCallback<String> {}

    /**
     * NDK part
     */
    static {
        System.loadLibrary("native-lib");
    }

    native int amplitudesFromAudioJNI(String pathToAudio, String txtCache, String audioCache);

}
