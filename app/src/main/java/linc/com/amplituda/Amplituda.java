package linc.com.amplituda;

import android.content.Context;

public class Amplituda {

    static {
        System.loadLibrary("native-lib");
    }

    public void init(SuccessCallback successCallback) {
//        System.out.println("AMPLITUDE ===================== " + stringFromJNI());
        successCallback.onSuccess(stringFromJNI("/storage/emulated/0/viber/kygo.mp3"));
        //    const char *filename = "/storage/emulated/0/viber/kygo.mp3";
//    const char *filename = "/storage/emulated/0/viber/ex.wav";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/music/f_voip_dur.opus";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/music/Голос 0017325996153317080688.m4a";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/video_notes/5406735884265457666.mp4";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/voice/5382102159468791418.oga";
//    const char *filename = "/storage/emulated/0/Android/data/org.thunderdog.challegram/files/music/DiscDj_Rec_2020-06-21_18-38-44.mp3";
        // SUPPORTED AUDIO FORMATS
        // [mp3, opus, oga, ogg, m4a, mp4] / 1h audio processing = 30sec

    }

    public Amplituda withContext(Context context) {
        return this;
    }
    public Amplituda fromFile() {
        return this;
    }
    public Amplituda fromPath() {
        return this;
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    native String stringFromJNI(String pathToAudio);

    public interface SuccessCallback {
        void onSuccess(String log);
    }

}
