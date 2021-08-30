package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import linc.com.amplituda.Amplituda;
import linc.com.amplituda.AmplitudaResult;
import linc.com.amplituda.Compress;
import linc.com.amplituda.InputAudio;

public class MainActivity extends AppCompatActivity {

    /*

    E/AMPLITUDA: current idx = 398532
    frames = 40875
    progress = 975
I/System.out: On progress: 975
E/AMPLITUDA: Processing time: 39,3110 seconds
I/System.out: Duration: 9571
    Data size: 398822

    -- kygo
     */

    // TODO: big data test
    // TODO: multithreading test
    // TODO: amplitudesPerSecond refactor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(this);

        amplituda.setLogConfig(Log.ERROR, true);

        Compress c = Compress.withParams(Compress.AVERAGE, 1);

//        amplituda.processAudio("/storage/emulated/0/Music/set.mp3")
//        amplituda.processAudio("/storage/emulated/0/Music/Linc - Amplituda.mp3")
        amplituda.processAudio("/storage/9016-4EF8/MUSIC/Hosini - Froozen.mp3")
//        amplituda.processAudio("/storage/9016-4EF8/MUSIC/Kygo - Broken Glass.mp3")
                .get(result -> {
                    List<Integer> amplitudesData = result.amplitudesAsList();
//                    List<Integer> amplitudesForFirstSecond = result.amplitudesForSecond(1);
                    long duration = result.getAudioDuration(AmplitudaResult.DurationUnit.SECONDS);
//                    String source = result.getAudioSource();
//                    InputAudio.Type sourceType = result.getInputAudioType();
                    // etc
                    //14585
                    System.out.println("Duration: " + duration);
                    System.out.println("Data size: " + amplitudesData.size());
                    /*try {
                        FileWriter fw = new FileWriter(new File("/storage/emulated/0/Music/amps.txt"));
                        fw.write(result.amplitudesAsSequence(AmplitudaResult.SequenceFormat.NEW_LINE));
                        fw.flush();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
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
