package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.FileWriter;

import linc.com.amplituda.Amplituda;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Amplituda amplituda = new Amplituda(getApplicationContext());

        amplituda.fromFile("/storage/emulated/0/Music/kygo.mp3");
        amplituda.setErrorListener(error -> {
            error.printStackTrace();
        });
        amplituda.compressAmplitudes(1);
        amplituda.amplitudesAsList(amps -> {
            System.out.println(amps.size());
        });
        amplituda.amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, amps -> {
            try {
                FileWriter fw = new FileWriter("/storage/emulated/0/Music/amps.txt");
                fw.write(amps);
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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
