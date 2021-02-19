package linc.com.amplituda;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


final class FileManager {

    private static String cache;
    static final String TXT_TEMP = "amplituda_tmp_text.txt";
    static final String AUDIO_TEMP = "amplituda_tmp_audio.mp3";

    static void init(Context context) {
        if(cache == null) {
            cache = context.getCacheDir().getPath() + File.separator;
        }
    }

    synchronized static String provideTempFile(final String temp) {
        return cache + temp;
    }

    synchronized static void clearCache() {
        deleteFile(cache + TXT_TEMP);
        deleteFile(cache + AUDIO_TEMP);
    }

    synchronized static String readFile(final String path) {
        StringBuilder output = new StringBuilder();
        try {
            FileReader reader = new FileReader(path);
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while( (line = buffer.readLine()) != null){
                output.append(line).append("\n");
            }
            reader.close();
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    synchronized static void deleteFile(final String path) {
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }
    }

}
