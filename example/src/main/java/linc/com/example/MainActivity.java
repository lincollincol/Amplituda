package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

import linc.com.amplituda.Amplituda;
import linc.com.amplituda.AmplitudaProcessingOutput;
import linc.com.amplituda.AmplitudaResult;
import linc.com.amplituda.InputAudio;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(this);
        amplituda.setLogConfig(Log.DEBUG, true);

        AmplitudaResult<String> localPathResult = amplituda.processAudio("/storage/emulated/0/Music/kygo.mp3")
                .get();
        printResult(localPathResult);

        AmplitudaResult<File> localFileResult = amplituda.processAudio(new File("/storage/emulated/0/Music/kygo.mp3"))
                .get();
        printResult(localFileResult);


        Thread urlTask = new Thread(() -> {
            AmplitudaResult<String> urlResult = amplituda.processAudio("http://commondatastorage.googleapis.com/codeskulptor-assets/Evillaugh.ogg")
                    .get();
            printResult(urlResult);
        });
        urlTask.start();
        /*try {
            urlTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        AmplitudaResult<Integer> resourceResult = amplituda.processAudio(R.raw.clap)
                .get();
        printResult(resourceResult);

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
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE),
                result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.SINGLE_LINE, " * ")
        );
    }

}
