package linc.com.amplituda;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AmplitudaResult<T> {

    private final String amplitudes;
    private final InputAudio<T> inputAudio;

    AmplitudaResult(
            final String amplitudes,
            final InputAudio<T> inputAudio
    ) {
        this.amplitudes = amplitudes;
        this.inputAudio = inputAudio;
    }

    /**
     * Returns input audio source: String (path/url), File or Integer resource
     */
    public T getAudioSource() {
        return inputAudio.getSource();
    }

    /**
     * Returns duration from file in seconds or millis
     * @param unit - output time unit: SECONDS or MILLIS
     */
    public long getAudioDuration(DurationUnit unit) {
        if (unit == DurationUnit.SECONDS) {
            return inputAudio.getDuration() / 1000;
        }
        return inputAudio.getDuration();
    }

    /**
     * Returns input audio type: URL, RESOURCE or FILE
     */
    public InputAudio.Type getInputAudioType() {
        return inputAudio.getType();
    }

    /**
     * Convert result amplitudes to List
     */
    public List<Integer> amplitudesAsList() {
        if(amplitudes == null || amplitudes.isEmpty())
            return Collections.emptyList();

        String[] log = amplitudes.split("\n");
        List<Integer> amplitudes = new ArrayList<>();

        for (String amplitude : log) {
            if(amplitude.isEmpty()) {
                break;
            }
            amplitudes.add(Integer.valueOf(amplitude));
        }
        return amplitudes;
    }

    /**
     * Convert result amplitudes to JSON format
     */
    public String amplitudesAsJson() {
        if(amplitudes == null || amplitudes.isEmpty())
            return "";
        return Arrays.toString(amplitudesAsList().toArray());
    }

    /**
     * Overload for amplitudesAsSequence method. Use space (" ") as a default delimiter
     * @param format - output format: single line or multiline output string
     */
    public String amplitudesAsSequence(final SequenceFormat format) {
        if(amplitudes == null || amplitudes.isEmpty())
            return "";
        return amplitudesAsSequence(format, " ");
    }

    /**
     * Convert result amplitudes to single line string with custom delimiter and send result to user via stringCallback
     * @param format - output format: single line or multiline output string
     * @param singleLineDelimiter - delimiter between amplitudes. WARNING: this parameter will be ignored when NEW_LINE_SEQUENCE_FORMAT passed as a parameter
     */
    public String amplitudesAsSequence(
            final SequenceFormat format,
            final String singleLineDelimiter
    ) {
        if(amplitudes == null || amplitudes.isEmpty())
            return "";

        if (format == SequenceFormat.SINGLE_LINE) {
            return amplitudesToSingleLineSequence(
                    amplitudes,
                    singleLineDelimiter
            );
        }
        return amplitudes;
    }

    /**
     * Extracts list of amplitudes per specific second
     * @param second - specific second from input file
     */
    public List<Integer> amplitudesForSecond(final int second) {
        List<Integer> data = amplitudesAsList();
        final int duration = (int) getAudioDuration(DurationUnit.SECONDS);

        if(second > duration || duration == 0) {
            return Collections.emptyList();
        }

        // amplitudes per second
        final int aps = (data.size() / duration);

        int index = (second * data.size()) / duration;

        List<Integer> amplitudesForSecond = new ArrayList<>();

        for(int i = index; i > index - aps; i--) {
            if(i < 0 || i >= data.size())
                break;
            amplitudesForSecond.add(data.get(i));
        }
        Collections.reverse(amplitudesForSecond);
        return amplitudesForSecond;
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

    public enum  DurationUnit {
        SECONDS, MILLIS
    }

    public enum SequenceFormat {
        SINGLE_LINE, NEW_LINE
    }

}
