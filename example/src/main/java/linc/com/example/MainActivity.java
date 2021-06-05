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
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import linc.com.amplituda.Amplituda;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(this);

//        amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")


        /*Observable.create(emitter -> {
            try {
                for(int i = 0; i < 5; i++) {
                    long start = System.currentTimeMillis();
                    amplituda.fromPath("/storage/emulated/0/Music/ncs_hr.mp3");
                    System.out.println();
                    emitter.onNext("Time = " + ((System.currentTimeMillis() - start) / 1000));
                }
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io())
        .subscribe(
                time -> {
                    System.out.println("NEXT");
                    System.out.println(time);
                },
                error -> {
                    System.out.println("ERROR");
                    error.printStackTrace();
                },
                () -> {
                    System.out.println("COMPLETE");
                });*/


        amplituda.fromPath("/storage/emulated/0/Music/ncs_hr.mp3")
                .amplitudesAsJson(json -> {
                    System.out.println("As json: " + json);
                })
                /*.amplitudesAsList(list -> {
                    System.out.print("As list: ");
                    for(int tmp : list) {
                        System.out.print(tmp + " ");
                    }
                    System.out.println();
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
                })*/;

    }
}
