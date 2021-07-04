package linc.com.amplituda;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;


final class FileManager {

    private String stashedPath;
    private final String cache;
    static final String RAW_TEMP = "amplituda_tmp_raw";

    FileManager(Context context) {
        cache = context.getCacheDir().getPath() + File.separator;
    }

    synchronized void clearCache() {
        deleteFile(cache + RAW_TEMP);
    }

    synchronized void deleteFile(final String path) {
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }
    }

    synchronized void stashPath(String path) {
        stashedPath = path;
    }

    synchronized String getStashedPath() {
        return stashedPath;
    }

    synchronized boolean isAudioFile(String path) {
        try {
            getAudioDuration(path);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    synchronized long getAudioDuration(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        return Long.parseLong(mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
        ));
    }

    synchronized File getRawFile(int resource, Resources resources) {
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
