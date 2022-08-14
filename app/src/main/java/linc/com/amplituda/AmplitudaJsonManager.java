package linc.com.amplituda;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

class AmplitudaJsonManager {

    /*
    "Audio info:\n" +
                        "millis = %d\n" +
                        "seconds = %d\n\n" +
                        "source = %s\n" +
                        "source type = %s\n\n" +
                        "Amplitudes:\n" +
                        "size: = %d\n" +
                        "list: = %s\n" +
                        "amplitudes for second 1: = %s\n" +
                        "json: = %s\n" +
                        "single line sequence = %s\n" +
                        "new line sequence = %s\n" +
                        "custom delimiter sequence = %s\n%n",
     */
    private final static String KEY_SOURCE = "source";
    private final static String KEY_SOURCE_TYPE = "source-type";
    private final static String KEY_SIZE = "size";
    private final static String KEY_DURATION = "duration";
    private final static String KEY_AMPLITUDES = "amplitudes";

    static String toJson(AmplitudaResult<?> result) {
        try {
            JSONObject root = new JSONObject();
            root.put(KEY_SOURCE, result.getAudioSource().toString());
            root.put(KEY_SOURCE_TYPE, result.getInputAudioType().name());
            root.put(KEY_SIZE, result.amplitudesAsList().size());
            root.put(KEY_DURATION, result.getAudioDuration(AmplitudaResult.DurationUnit.MILLIS));
            root.put(KEY_AMPLITUDES, result.amplitudesAsJson());
            return root.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    static AmplitudaResult<?> fromJson(String data) {
        try {
            JSONObject root = new JSONObject(data);
            String source = root.getString(KEY_SOURCE);
            String sourceType = root.getString(KEY_SOURCE_TYPE);
            String amplitudes = root.getString(KEY_AMPLITUDES)
                    .replaceAll("\\[\\]", "")
                    .replaceAll(",", "\n");
            int size = root.getInt(KEY_SIZE);
            long duration = root.getLong(KEY_DURATION);

            return new AmplitudaResult<>(
                    amplitudes,
                    new InputAudio<Object>(source, duration)
            );
        } catch (JSONException e) {
            return null;
        }
    }


}
