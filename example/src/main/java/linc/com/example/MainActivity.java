package linc.com.example;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import linc.com.amplituda.Amplituda;
import linc.com.amplituda.AmplitudaProgressListener;
import linc.com.amplituda.AmplitudaResult;
import linc.com.amplituda.Cache;
import linc.com.amplituda.Compress;
import linc.com.amplituda.InputAudio;
import linc.com.amplituda.ProgressOperation;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> processAudio()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
        permissionLauncher.launch(permission);
    }

    private void processAudio() {
        Amplituda amplituda = new Amplituda(this);
        amplituda.setLogConfig(Log.ERROR, true);
        amplituda.processAudio(
                "/storage/emulated/0/Music/Linc - Amplituda.mp3",
                Compress.withParams(Compress.PEAK, 1),
                Cache.withParams(Cache.REFRESH),
                new AmplitudaProgressListener() {
                    @Override
                    public void onStartProgress() {
                        super.onStartProgress();
                        System.out.println("Start Progress");
                    }
                    @Override
                    public void onStopProgress() {
                        super.onStopProgress();
                        System.out.println("Stop Progress");
                    }
                    @Override
                    public void onProgress(ProgressOperation operation, int progress) {
                        String currentOperation = "";
                        switch (operation) {
                            case PROCESSING: currentOperation = "Process audio"; break;
                            case DECODING: currentOperation = "Decode audio"; break;
                            case DOWNLOADING: currentOperation = "Download audio from url"; break;
                        }
                        System.out.printf("%s: %d%% %n", currentOperation, progress);
                    }
                }
        ).get(this::printResult, Throwable::printStackTrace);
    }

    private void printResult(AmplitudaResult<?> result) {
        System.out.printf(Locale.US,
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
                result.getAudioDuration(AmplitudaResult.DurationUnit.MILLIS),
                result.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS),
                result.getInputAudioType() == InputAudio.Type.FILE ? ((File) result.getAudioSource()).getAbsolutePath() : result.getAudioSource(),
                result.getInputAudioType().name(),
                result.amplitudesAsList().size(),
                Arrays.toString(result.amplitudesAsList().toArray()),
                Arrays.toString(result.amplitudesForSecond(1).toArray()),
                result.amplitudesAsJson(),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.NEW_LINE),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE, " * ")
        );
    }

}