package linc.com.amplituda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

final class FileManager {

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
