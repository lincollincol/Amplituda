package linc.com.amplituda;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;


final class FileManager {

    private Resources resources;
    private String stashedPath;
    private String cache;
    static final String RAW_TEMP = "amplituda_tmp_raw";

    boolean cacheNotNull() {
        return cache != null;
    }

    /**
     * Init cache directory path
     */
    synchronized void initCache(final Context context) {
        cache = context.getCacheDir().getPath() + File.separator;
    }

    /**
     * Init resources for res/raw decoding
     */
    synchronized void initResources(final Context context) {
        resources = context.getResources();
    }

    /**
     * Delete local storage file
     */
    synchronized void deleteFile(final File file) {
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
    synchronized long getAudioDuration(final String path) {
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
    synchronized File getRawFile(final int resource) {
        File temp = new File(cache, RAW_TEMP);
        streamToFile(resources.openRawResource(resource), temp, 1024 * 4);
        return guessAudioExtension(temp);
    }

    /**
     * Copy audio from URL to local storage
     * @param audioUrl - audio file url
     * @return audio file from local storage
     */
    synchronized File getUrlFile(final String audioUrl) {
        File temp = new File(String.format(
                Locale.US,
                "%s%s.%s",
                cache,
                RAW_TEMP,
                MimeTypeMap.getFileExtensionFromUrl(audioUrl)
        ));

        try {
            URL url = new URL(audioUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            streamToFile(
                    new BufferedInputStream(url.openStream()),
                    temp,
                    1024
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return temp;
    }

    /**
     * Copy audio from URL to local storage
     * @param inputStream - audio file input stream
     * @param temp - cache file to which the stream will be written
     * @param bufferSize - copy operation buffer size
     */
    private void streamToFile(final InputStream inputStream, final File temp, final int bufferSize) {
        try {
            OutputStream fos = new FileOutputStream(temp);
            byte[] buffer = new byte[bufferSize];
            int read = 0;

            while ((read = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }

            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException | NullPointerException ignored) {
            }
        }
    }

    /**
     * Copy audio from URL to local storage
     * @param inputAudio - audio file without extension
     * @return null or audio file with valid extension
     */
    private synchronized File guessAudioExtension(final File inputAudio) {
        if(inputAudio == null) {
            return null;
        }

        // Choose correct extension
        for(AudioExtension extension : AudioExtension.values()) {
            // Rename temp with extension
            File temp = new File(String.format("%s.%s", inputAudio.getPath(), extension.name()));
            inputAudio.renameTo(temp);

            // Validate audio with current extension
            if(isAudioFile(temp.getPath())) {
                return temp;
            }
        }

        // File is not supported
        deleteFile(inputAudio);

        return null;
    }

    private enum AudioExtension {
        mp3, wav, ogg, opus, acc, wma, flac, mp4, mp1, mp2, m4a
    }
}