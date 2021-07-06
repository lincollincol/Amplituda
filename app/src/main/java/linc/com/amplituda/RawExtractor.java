package linc.com.amplituda;

import android.content.Context;
import android.content.res.Resources;

import java.io.File;

final class RawExtractor {

    private final Resources resources;
    private final FileManager fileManager;

    RawExtractor(Context context, final FileManager fileManager) {
        this.resources = context.getResources();
        this.fileManager = fileManager;
    }

    /**
     * Get res/raw file and choose valid extension for file
     * @return audio file with extension. Otherwise return null in case when file is not audio
     */
    File getAudioFromRawResources(final int rawId) {
        // Copy resId file from res/raw to local storage without extension
        File rawAudio = fileManager.getRawFile(rawId, resources);

        if(rawAudio == null) {
            return null;
        }

        // Choose correct extension
        for(AudioExtension extension : AudioExtension.values()) {
            // Rename temp with extension
            File temp = new File(String.format("%s.%s", rawAudio.getPath(), extension.name()));
            rawAudio.renameTo(temp);

            // Validate audio with current extension
            if(fileManager.isAudioFile(temp.getPath())) {
                return temp;
            }
        }

        // File is not supported
        fileManager.deleteFile(rawAudio);

        return null;
    }

    private enum AudioExtension {
        mp3, wav, ogg, opus, acc, wma, flac, mp4, mp1, mp2, m4a
    }


}
