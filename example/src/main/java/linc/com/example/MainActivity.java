package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import leakcanary.LeakCanary;
import linc.com.amplituda.Amplituda;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(getApplicationContext());

//        amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")

        Observable.create(emitter -> {
            try {
                for(int i = 0; i < 1; i++) {
                    long start = System.currentTimeMillis();
                    emitter.onNext("File #" + i);
//                    amplituda.fromPath("/storage/emulated/0/Music/ncs_hr.mp3")
                    amplituda.fromPath("/storage/emulated/0/Music/queen.mp3")
                        .amplitudesAsJson(json -> {
                            emitter.onNext("Time = " + ((System.currentTimeMillis() - start) / 1000) + " = " + json);
                        })
                    .releaseCurrent();
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
                });


//        amplituda.fromPath("/storage/emulated/0/Music/ncs_hr.mp3") // 137816
//        amplituda.fromPath("/storage/emulated/0/Music/kygo.mp3") // 7810
//        amplituda.fromPath("/storage/emulated/0/Music/blt.mp3") // 14699
//                .amplitudesAsJson(json -> {
//                    System.out.println("As json: " + json);
//                })
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
