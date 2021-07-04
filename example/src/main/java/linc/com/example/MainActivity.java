package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import linc.com.amplituda.Amplituda;
import linc.com.amplituda.exceptions.AmplitudaException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(getApplicationContext());

        /*String[] files = new String[] {
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/kygo.mp3",
                "/storage/emulated/0/Music/dwv.mp4",
                "/storage/emulated/0/Music/kygo_s16.wav",
                "/storage/emulated/0/Music/echo.mp3",
                "/storage/emulated/0/Music/kygo_u8.wav",
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/igor.wav",
                "/storage/emulated/0/Music/Jain.mp3",
                "/storage/emulated/0/Music/clap.mp3",
                "/storage/emulated/0/Music/kygo.mp3",
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/log.wav",
                "/storage/emulated/0/Music/log.mp3",
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/igor.mp3",
                "/storage/emulated/0/Music/kygo_pcm.wav",
                "/storage/emulated/0/Music/clap.wav",
                "/storage/emulated/0/Music/clap.mp3"
        };


        for(int i = 0; i < files.length; i++) {
            System.out.println("--------------------------- Process index: " + i + " ---------------------------");
            System.out.println("File: " + files[i]);
            amplituda.fromFile(files[i])
                    .setLogConfig(Log.ERROR, true)
                    .amplitudesAsJson(amps -> {
                        System.out.println("Success data: " + amps);
                    })
                    .setErrorListener(amplitudesResult -> {
                        System.out.println("Error message: " + amplitudesResult.getLocalizedMessage());
                    });
        }*/


        /*amplituda.fromFile(R.raw.kygo_pcm)
                .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, amps -> {
                    try {
                        FileWriter fw = new FileWriter(new File("/storage/emulated/0/Music/amps.txt"));
                        fw.write(amps);
                        fw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .amplitudesAsJson(System.out::println);*/


        String[] files = new String[] {
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/kygo.mp3",
                "/storage/emulated/0/Music/dwv.mp4",
                "/storage/emulated/0/Music/kygo_s16.wav",
                "/storage/emulated/0/Music/echo.mp3",
                "/storage/emulated/0/Music/kygo_u8.wav",
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/igor.wav",
                "/storage/emulated/0/Music/Jain.mp3",
                "/storage/emulated/0/Music/clap.mp3",
                "/storage/emulated/0/Music/kygo.mp3",
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/log.wav",
                "/storage/emulated/0/Music/log.mp3",
                "/storage/emulated/0/Music/first_wave.png",
                "/storage/emulated/0/Music/igor.mp3",
                "/storage/emulated/0/Music/kygo_pcm.wav",
                "/storage/emulated/0/Music/clap.wav",
                "/storage/emulated/0/Music/clap.mp3"
        };

        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            for(int i = 0; i < 10; i++) {
                System.out.println("--------------------------- Process index: " + i + " ---------------------------");
//                System.out.println("File: " + files[i]);
                amplituda.fromFile("/storage/emulated/0/Music/ncs_hr.mp3")
                        .setLogConfig(Log.ERROR, true)
                        .amplitudesAsJson(amps -> {
//                            System.out.println("Success data: " + amps);
                            emitter.onNext(amps);
                        })
                        .setErrorListener(error -> {
//                            System.out.println("Error message: " + amplitudesResult.getLocalizedMessage());
                            emitter.onError(error);
                        });
            }
            emitter.onComplete();
        })
        .subscribeOn(Schedulers.io())
        .subscribe(
                data -> {
                    System.out.println("Success data: " + data);
                },
                error -> {
                    System.out.println("Error message: " + error.getLocalizedMessage());
                },
                () -> {
                    System.out.println("Complete!");
                });
//        amplituda.fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")

        /*Observable.create(emitter -> {
            try {
                for(int i = 0; i < 1; i++) {
                    long start = System.currentTimeMillis();
//                    emitter.onNext("File #" + i);
//                    amplituda.fromPath("/storage/emulated/0/Music/first_wave.png")
//                    amplituda.fromPath("/storage/emulated/0/Music/kygo.mp3")
//                    amplituda.fromPath("/storage/emulated/0/Music/dwv.mp4")
//                    amplituda.fromPath("/storage/emulated/0/Music/kygo_s16.wav")
//                    amplituda.fromPath("/storage/emulated/0/Music/kygo_u8.wav")
//                    amplituda.fromPath("/storage/emulated/0/Music/igor.wav")
//                    amplituda.fromPath("/storage/emulated/0/Music/Jain.mp3")
//                    amplituda.fromPath("/storage/emulated/0/Music/kygo.mp3")
//                    amplituda.fromPath("/storage/emulated/0/Music/kygo_pcm.wav")
//                    amplituda.fromPath("/storage/emulated/0/Music/clap.wav")
//                    amplituda.fromPath("/storage/emulated/0/Music/clap.mp3")
//                            .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, emitter::onNext);
//                        .amplitudesAsJson(json -> {
//                            emitter.onNext("Time = " + ((System.currentTimeMillis() - start) / 1000f) + " = " + json);
//                        });
                }
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io())
        .subscribe(
                time -> {

//                    FileWriter fw = new FileWriter(new File("/storage/emulated/0/Music/amps.txt"));
//                    fw.write(time.toString());
//                    fw.close();
                    // 2147483647
                    // ----397733
                    System.out.println(time.toString());
                    System.out.println("RESULT SIZE = " + time.toString().length());
                },
                error -> {
                    System.out.println("ERROR");
                    error.printStackTrace();
                },
                () -> {
                    System.out.println("COMPLETE");
                });*/



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
                });*/

    }
}
