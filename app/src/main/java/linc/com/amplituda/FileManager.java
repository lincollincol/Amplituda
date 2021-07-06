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

    /**
     * Delete local storage file
     */
    synchronized void deleteFile(File file) {
        if(file != null && file.exists()) {
            file.delete();
        }
    }

    /**
     * Stash path to file
     */
    synchronized void stashPath(final String path) {
        stashedPath = path;
    }

    /**
     * Clear stashed path
     */
    synchronized void clearStashedPath() {
        stashedPath = "";
    }

    /**
     * Return stashed path
     */
    synchronized String getStashedPath() {
        return stashedPath;
    }

    /**
     * Validate audio file
     * @param path - audio file path
     * @return true when file with path is audio file.
     */
    synchronized boolean isAudioFile(final String path) {
        try {
            getAudioDuration(path);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Get duration from audio file in seconds
     */
    synchronized long getAudioDuration(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        return Long.parseLong(mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
        ));
    }

    /**
     * Copy res/raw file to local storage
     * @param resource - res/raw file id
     * @return raw file from local storage
     */
    synchronized File getRawFile(final int resource, Resources resources) {
        InputStream inputStream = null;
        File temp = null;
        try {
            inputStream = resources.openRawResource(resource);
            temp = new File(cache, RAW_TEMP);
            FileOutputStream fio = new FileOutputStream(temp);
            byte[] buffer = new byte[1024 * 4];
            int read = 0;

            while( (read = inputStream.read(buffer)) != -1) {
                fio.write(buffer, 0, read);
            }

            fio.close();
        } catch (IOException | RuntimeException ignored) {
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException | NullPointerException ignored) {
            }
        }
        return temp;
    }

}
