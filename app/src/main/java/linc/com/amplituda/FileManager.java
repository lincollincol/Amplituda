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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;


final class FileManager {

    static final String RAW_TEMP = "amplituda_tmp_raw";
    private final Resources resources;
    private final String cache;

    FileManager(final Context context) {
        resources = context.getResources();
        cache = context.getCacheDir().getPath() + File.separator;
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
    synchronized File getRawFile(final int resource, final AmplitudaProgressListener listener) {
        File temp = new File(cache, RAW_TEMP);
        try {
            InputStream inputStream = resources.openRawResource(resource);
            streamToFile(inputStream, temp, 1024 * 4, inputStream.available(), listener);
            return guessAudioExtension(temp);
        } catch (Resources.NotFoundException | IOException ignored) {
            return null;
        }
    }

    /**
     * Copy audio from URL to local storage
     * @param audioUrl - audio file url
     * @return audio file from local storage
     */
    synchronized File getUrlFile(final String audioUrl, final AmplitudaProgressListener listener) {
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
                    1024,
                    getUrlContentLength(connection),
                    listener
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
    private void streamToFile(
            final InputStream inputStream,
            final File temp,
            final int bufferSize,
            final long contentLength,
            final AmplitudaProgressListener listener
    ) {
        try {
            OutputStream fos = new FileOutputStream(temp);
            byte[] buffer = new byte[bufferSize];
            int read;
            int bytesWritten = 0, progress = 0;

            while ((read = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
                bytesWritten += read;

                if(listener != null && contentLength > 0) {
                    int current_progress = (int) (((float) bytesWritten / (float) contentLength) * 100.0);
                    if(current_progress != progress) {
                        listener.onProgressInternal(current_progress);
                        progress = current_progress;
                    }
                }
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

    private synchronized long getUrlContentLength(URLConnection url) {
        try {
            final HttpURLConnection urlConnection = (HttpURLConnection) url;
            urlConnection.setRequestMethod("HEAD");
            final String lengthHeaderField = urlConnection.getHeaderField("content-length");
            Long result = lengthHeaderField == null ? null : Long.parseLong(lengthHeaderField);
            return result == null || result < 0L ? -1L : result;
        } catch (Exception ignored) {
        }
        return -1L;
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