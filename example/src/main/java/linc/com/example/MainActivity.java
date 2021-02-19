package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import linc.com.amplituda.Amplituda;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(this);

//        amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")

        // 4.21
        // millis = 261564
        // sec = 261
        // frames = 10013


        amplituda.fromPath("/storage/emulated/0/Music/kygo.wav")
                .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, sequence -> {
                    try {
                        FileWriter fw = new FileWriter(new File("/storage/emulated/0/Music/amps.txt"));
                        fw.write(sequence);
                        fw.flush();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    System.out.println(sequence);
                });

//        amplituda.fromPath("/storage/emulated/0/Music/dream.mp3")
//                /*.amplitudesAsJson(json -> {
//                    System.out.println("As json ====== " + json);
//                })*/
//                .amplitudesAsList(list -> {
//                    int duration = getDurationSeconds("/storage/emulated/0/Music/dream.mp3");
//                    int fps = list.size() / duration; // frames per second
//
//                    // Use second as a map key
//                    int currentSecond = 0;
//                    // Map with format = Map<Second, Amplitudes>
//                    Map<Integer, List<Integer>> amplitudes = new LinkedHashMap<>();
//                    // Temporary amplitudes list
//                    List<Integer> amplitudesPerSecond = new ArrayList<>();
//
//                    for(int frameIndex = 0; frameIndex < list.size(); frameIndex++) {
//                        if(frameIndex % fps == 0) { // Save all amplitudes when current frame index equals to fps
//                            // Save amplitudes to map
//                            amplitudes.put(currentSecond, new ArrayList<>(amplitudesPerSecond));
//                            // Clear temporary amplitudes
//                            amplitudesPerSecond.clear();
//                            // Increase current second
//                            currentSecond++;
//                        } else {
//                            // Add amplitude to temporary list
//                            amplitudesPerSecond.add(list.get(frameIndex));
//                        }
//                    }
//
//                    for(int i = 0; i < amplitudes.size(); i++) {
//                        System.out.println(String.format(
//                                Locale.getDefault(),
//                                "Second %d: %s", i, Arrays.toString(amplitudes.get(i).toArray()))
//                        );
//                    }
//
//
//
//                    /*System.out.print("As list ====== ");
//                    for(int tmp : list) {
//                        System.out.print(tmp + " ");
//                    }
//                    System.out.println();*/
//                });
                /*.amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, defSeq -> {
                    System.out.println("As sequence default ====== " + defSeq);
                })
                .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, " * ", custSeq -> {
                    System.out.println("As sequence custom ====== " + custSeq);
                })
                .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, newLineSeq -> {
                    System.out.println("As new line sequence ====== " + newLineSeq);
                });*/

    }

    private long getDurationMillis(String audio) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(audio);
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(durationStr);
    }

    private int getDurationSeconds(String audio) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(audio);
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Integer.parseInt(durationStr) / 1000;
    }
}
