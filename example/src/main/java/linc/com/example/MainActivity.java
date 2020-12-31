package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import linc.com.amplituda.Amplituda;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        new Amplituda().fromPath("/storage/emulated/0/Music/Linc - Amplituda.mp3")
//        new Amplituda().fromPath("/storage/9016-4EF8/MUSIC/Worakls - Red Dressed (Ben Böhmer Remix).mp3")

/*
            if (decode_audio_file("/storage/emulated/0/Music/clap_effect.mp3", sample_rate, &data, &size) != 0) {
    if (decode_audio_file("/storage/9016-4EF8/MUSIC/Kygo - Broken Glass.mp3", sample_rate, &data, &size) != 0) {
    if (decode_audio_file("/storage/9016-4EF8/MUSIC/Worakls - Red Dressed (Ben Böhmer Remix).mp3", sample_rate, &data, &size) != 0) {
        if (decode_audio_file("/storage/9016-4EF8/MUSIC/London Grammar - Strong (Yotto Rework).mp3", sample_rate, &data, &size) != 0) {
    if (decode_audio_file("/storage/emulated/0/Music/kygo.wav", sample_rate, &data, &size) != 0) {
        */
//        new Amplituda().fromPath("/storage/emulated/0/Music/clap_effect.mp3")
        new Amplituda().fromPath("/storage/emulated/0/Music/kygo.wav")
                .amplitudesAsJson(json -> {
                    System.out.println("As json ====== " + json);
                })/*.amplitudesAsList(list -> {
                    System.out.print("As list ====== ");
                    for(int tmp : list) {
                        System.out.print(tmp + " ");
                    }
                    System.out.println();
                })
                .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, defSeq -> {
                    System.out.println("As sequence default ====== " + defSeq);
                })
                .amplitudesAsSequence(Amplituda.SINGLE_LINE_SEQUENCE_FORMAT, " * ", custSeq -> {
                    System.out.println("As sequence custom ====== " + custSeq);
                })
                .amplitudesAsSequence(Amplituda.NEW_LINE_SEQUENCE_FORMAT, newLineSeq -> {
                    System.out.println("As new line sequence ====== " + newLineSeq);
                })*/;

    }
}
