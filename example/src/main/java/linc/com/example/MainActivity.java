package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import linc.com.amplituda.Amplituda;
import linc.com.amplituda.AmplitudaProcessingOutput;
import linc.com.amplituda.AmplitudaResult;
import linc.com.amplituda.InputAudio;
import linc.com.amplituda.callback.AmplitudaSuccessListener;
import linc.com.amplituda.exceptions.AmplitudaException;
import linc.com.amplituda.exceptions.io.AmplitudaIOException;
import linc.com.amplituda.exceptions.processing.AmplitudaProcessingException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(this);

//        amplituda.processAudio("/storage/emulated/0/Music/Linc - Amplituda.mp3")
        amplituda.processAudio("/storage/9016-4EF8/MUSIC/Hosini - Froozen.mp3")
                .get(result -> {
                    List<Integer> amplitudesData = result.amplitudesAsList();
                    List<Integer> amplitudesForFirstSecond = result.amplitudesForSecond(1);
                    long duration = result.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS);
                    String source = result.getAudioSource();
                    InputAudio.Type sourceType = result.getInputAudioType();
                    // etc
                }, exception -> {
                    exception.printStackTrace();
                });

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
                result.amplitudesAsJson(),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.NEW_LINE),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE, " * ")
        );
    }

}
