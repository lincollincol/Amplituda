package linc.com.amplituda;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UnknownFormatFlagsException;

public class Amplituda {

    public static int SINGLE_LINE_SEQUENCE_FORMAT = 0;
    public static int NEW_LINE_SEQUENCE_FORMAT = 1;

    private String resultLog;
    private Context context;

    public Amplituda(Context context) {

    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    public Amplituda fromFile(File audio) {
        fromPath(audio.getPath());
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audioPath - path to source file
     */
    public Amplituda fromPath(String audioPath) {
        resultLog = amplitudesFromAudioJNI(audioPath);
        return this;
    }

    /**
     * Convert result amplitudes to List
     * @param listCallback - result callback
     */
    public Amplituda amplitudesAsList(ListCallback listCallback) {
        String[] log = resultLog.split("\n");
        List<Integer> amplitudes = new ArrayList<>();
        for (String amplitude : log) {
            amplitudes.add(Integer.valueOf(amplitude));
        }
        listCallback.onSuccess(amplitudes);
        return this;
    }

    /**
     * Convert result amplitudes to JSON format
     * @param jsonCallback - result callback
     */
    public Amplituda amplitudesAsJson(StringCallback jsonCallback) {
        jsonCallback.onSuccess("[" + amplitudesToSingleLineSequence(resultLog, ", ") + "]");
        return this;
    }

    /**
     * Overload for amplitudesAsSequence method. Use space (" ") as a default delimiter
     * @param format - output format: single line or multiline output string
     * @param stringCallback - result callback
     */
    public Amplituda amplitudesAsSequence(int format, StringCallback stringCallback) {
        amplitudesAsSequence(format, " ", stringCallback);
        return this;
    }

    /**
     * Convert result amplitudes to single line string with custom delimiter and send result to user via stringCallback
     * @param format - output format: single line or multiline output string
     * @param singleLineDelimiter - delimiter between amplitudes. WARNING: this parameter will be ignored when NEW_LINE_SEQUENCE_FORMAT passed as a parameter
     * @param stringCallback - result callback
     */
    public Amplituda amplitudesAsSequence(int format, String singleLineDelimiter, StringCallback stringCallback) {
        switch (format) {
            case 0: {
                stringCallback.onSuccess(amplitudesToSingleLineSequence(resultLog, singleLineDelimiter));
                break;
            }
            case 1: {
                stringCallback.onSuccess(resultLog);
                break;
            }
            default: throw new UnknownFormatFlagsException("Use SINGLE_LINE_SEQUENCE_FORMAT or NEW_LINE_SEQUENCE_FORMAT as a parameter when you call amplitudesAsSequence!");
        }
        return this;
    }

    /**
     * Convert result amplitudes to single line string with delimiter
     * @param amplitudes - result from native c++ code
     * @param delimiter - amplitudes separator
     * @return string from amplitudes with custom delimiter. Example -> 0, 1, 2 | delimiter = ", "
     */
    private String amplitudesToSingleLineSequence(String amplitudes, String delimiter) {
        String[] log = amplitudes.split("\n");
        return TextUtils.join(delimiter, log);
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

    native String amplitudesFromAudioJNI(String pathToAudio);

}
