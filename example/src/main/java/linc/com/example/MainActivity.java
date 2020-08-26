package linc.com.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import linc.com.amplituda.Amplituda;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Amplituda().init((log) -> {
            System.out.println("MY log " + log);
        });
    }
}
