package linc.com.amplituda;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UnknownFormatFlagsException;

public final class Amplituda {

    public static final int SINGLE_LINE_SEQUENCE_FORMAT = 0;
    public static final int NEW_LINE_SEQUENCE_FORMAT = 1;
    private static final String AMPLITUDA_TMP_VALUES = "amplituda_tmp_values.txt";
    private static final String APP_TAG = "Amplituda";

    private final String temporaryAmplitudaDataFile;
    private String amplitudes;

    public Amplituda(Context context) {
        this.temporaryAmplitudaDataFile = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                .getPath() + File.separator + AMPLITUDA_TMP_VALUES;
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    public Amplituda fromFile(final File audio)  {
        if(!audio.exists()) {
            Log.e(APP_TAG, "Wrong file! Please check path and try again!");
        } else {
            int code = amplitudesFromAudioJNI(audio.getPath(), temporaryAmplitudaDataFile);
            if(code != 0) {
                Log.e(APP_TAG, "Something went wrong! Check error log with \"Amplituda\" tag!");
            }
            this.amplitudes = FileManager.readFile(temporaryAmplitudaDataFile);
            FileManager.deleteFile(temporaryAmplitudaDataFile);
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
        if(amplitudes == null)
            return this;
        String[] log = amplitudes.split("\n");
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

    native int amplitudesFromAudioJNI(String pathToAudio, String pathToCache);

}
