package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import linc.com.amplituda.Amplituda;
import linc.com.amplituda.AmplitudaCompressOutput;
import linc.com.amplituda.exceptions.AmplitudaException;
import linc.com.amplituda.exceptions.io.AmplitudaIOException;
import linc.com.amplituda.exceptions.processing.SecondOutOfBoundsException;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(getApplicationContext());

        amplituda.fromFile("/storage/emulated/0/Music/kygo.mp3");
        amplituda.setCompression(AmplitudaCompressOutput.FULL);
        amplituda.amplitudesAsJson(amps -> System.out.println(amps.length()));

/*
        for(int i = 0; i < amplituda.getDuration(Amplituda.SECONDS); i++) {
            amplituda.amplitudesPerSecond(i, amps -> {
                int sum = 0;
                for(int val : amps) {
                    sum += val;
                }
                if(sum == 0) {
                    ampls += String.valueOf(sum);
                } else {
                    ampls += String.valueOf(sum / amps.size());
                }
                ampls += "\n";
            });
        }
*/


        /*amplituda.fromFile("/storage/emulated/0/Music/kygo.mp3")
                .amplitudesAsJson(json -> {
                    System.out.println("As json: " + json);
                })
                .amplitudesAsList(list -> {
                    System.out.print("As list: ");
                    for(int tmp : list) {
                        System.out.print(tmp + " ");
                    }
                    System.out.println();
                })
                .setErrorListener(error -> {
                    if(error instanceof AmplitudaIOException) {
                        System.out.println("IO exception");
                    }
                })
                .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, defSeq -> {
                    System.out.println("As sequence default: " + defSeq);
                })
                .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, " * ", custSeq -> {
                    System.out.println("As sequence custom: " + custSeq);
                })
                .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, newLineSeq -> {
                    System.out.println("As new line sequence: " + newLineSeq);
                })
                .amplitudesPerSecond(5, list -> {
                    System.out.println("Amplitudes at second 5: " + Arrays.toString(list.toArray()));
                });*/

    }
}
