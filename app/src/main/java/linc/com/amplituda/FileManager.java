package linc.com.amplituda;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


final class FileManager {

    private static String cache;
    private static String runtime;
    static final String TXT_TEMP = "amplituda_tmp_text.txt";
    static final String AUDIO_TEMP = "amplituda_tmp_audio.mp3";

    static void init(Context context) {
        if(cache == null) {
            cache = context.getCacheDir().getPath() + File.separator;
        }

    }

    /*synchronized static String prepareData() {
        File parent = new File("/storage/emulated/0/Music/");
        File out = new File("/storage/emulated/0/Music/amplituda_tmp_full.txt");

        try {
            FileWriter writer = new FileWriter(out);

            List<File> tmp_files = Arrays.asList(parent.listFiles());

            Collections.sort(tmp_files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return extractInt(o1) - extractInt(o2);
                }

                int extractInt(File s) {
                    String num = s.getName().replaceAll("\\D", "");
                    // return 0 if no digits found
                    return num.isEmpty() ? 0 : Integer.parseInt(num);
                }
            });

            for (File file : tmp_files) {
                if(file.getName().startsWith("amplituda")) {
                    writer.append(readFile(file.getPath()));
                    file.delete();
                }
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return readFile(out.getPath());
    }*/

    static String provideTempFile(final String temp) {
        return cache + temp;
    }

    static void clearCache() {
        deleteFile(cache + TXT_TEMP);
        deleteFile(cache + AUDIO_TEMP);
        new File("/storage/emulated/0/Music/amplituda_tmp_full.txt").delete();
    }

    static String readFile(final String path) {
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

    static void deleteFile(final String path) {
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }
    }

    static void saveRuntimePath(String path) {
        runtime = path;
    }

    static String retrieveRuntimePath() {
        return runtime;
    }

}
