package linc.com.amplituda;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;

import java.io.File;

final class RawExtractor {

    private final Resources resources;
    private final FileManager fileManager;

    RawExtractor(Context context, FileManager fileManager) {
        this.resources = context.getResources();
        this.fileManager = fileManager;
    }

    public File getAudioFromRawResources(int rawId) {
        // Copy resId file from res/raw to local storage without extension
        File rawAudio = fileManager.getRawFile(rawId, resources);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        // Choose correct extension
        for(AudioExtension extension : AudioExtension.values()) {
            try {
                // Rename temp with extension
                File temp = new File(String.format("%s.%s", rawAudio.getPath(), extension.name()));
                rawAudio.renameTo(temp);

                // Validate audio with current extension
                mediaMetadataRetriever.setDataSource(temp.getPath());
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                return temp;
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    private enum AudioExtension {
        mp3, wav, ogg, opus, acc, wma, flac, mp4, mp1, mp2, m4a
    }


}
