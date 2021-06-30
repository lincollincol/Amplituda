package linc.com.amplituda;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;


final class FileManager {

    private static String cache;
    private static String runtime;
    static final String TXT_TEMP = "amplituda_tmp_text.txt";
    static final String RAW_TEMP = "amplituda_tmp_raw";
    static final String AUDIO_TEMP = "amplituda_tmp_audio.mp3";

    synchronized static void init(Context context) {
        if(cache == null) {
            cache = context.getCacheDir().getPath() + File.separator;
        }
    }

    synchronized static String provideTempFile(final String temp) {
        return cache + temp;
    }

    synchronized static void clearCache() {
        deleteFile(cache + TXT_TEMP);
        deleteFile(cache + RAW_TEMP);
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

    synchronized static void saveRuntimePath(String path) {
        runtime = path;
    }

    synchronized static String retrieveRuntimePath() {
        return runtime;
    }


    synchronized static File getRawFile(int resource, Resources resources) {
        InputStream inputStream = resources.openRawResource(resource);
        File temp = new File(cache, RAW_TEMP);

        try {
            FileOutputStream fio = new FileOutputStream(temp);
            byte buffer[] = new byte[1024 * 4];
            int read = 0;

            while( (read = inputStream.read(buffer)) != -1) {
                fio.write(buffer, 0, read);
            }
            fio.close();
        } catch (FileNotFoundException notFoundException) {
            notFoundException.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }

}
