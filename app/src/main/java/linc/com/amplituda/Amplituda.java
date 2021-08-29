package linc.com.amplituda;

import android.content.Context;
import android.webkit.URLUtil;

import java.io.File;

import linc.com.amplituda.callback.AmplitudaProgressListener;
import linc.com.amplituda.exceptions.*;
import linc.com.amplituda.exceptions.io.*;

public final class Amplituda {

    private final FileManager fileManager;

    public Amplituda(final Context context) {
        fileManager = new FileManager(context);
    }

    /**
     * Enable Amplituda logs for mor processing information
     * @param priority - android Log constant. For example Log.DEBUG
     * @param enable - turn on / off logs
     */
    public Amplituda setLogConfig(final int priority, final boolean enable) {
        AmplitudaLogger.enable(enable);
        AmplitudaLogger.priority(priority);
        return this;
    }

    /**
     * Calculate amplitudes from file
     * @param audio - source file
     */
    private synchronized <T> AmplitudaResultJNI processFileJNI(
            final File audio,
            final InputAudio<T> inputAudio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) throws AmplitudaException {
        // Process audio
        if(!audio.exists()) {
            throw new FileNotFoundException();
        }

        if(!fileManager.isAudioFile(audio.getPath())) {
            throw new FileOpenException();
        }

        // TODO: 29.08.21 check for Compress params

        // Save start time
        long startTime = System.currentTimeMillis();

        // Process input audio
        AmplitudaResultJNI result = amplitudesFromAudioJNI(
                audio.getPath(),
                compress == null ? Compress.NONE : compress.getType(),
                compress == null ? Compress.NONE : compress.getFramesPerSecond(),
                listener
        );

        // Save audio duration when file is valid and was processed
        inputAudio.setDuration(fileManager.getAudioDuration(audio.getPath()));

        // Log operation time
        AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PROCESSING, startTime);
        return result;
    }

    public AmplitudaProcessingOutput<File> processAudio(
            final File audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        InputAudio<File> inputAudio = new InputAudio<>(audio, InputAudio.Type.FILE);
        try {
            return new AmplitudaProcessingOutput<>(
                    processFileJNI(audio, inputAudio, compress, listener),
                    inputAudio
            );
        } catch (AmplitudaException exception) {
            // Handle processing error
            return new AmplitudaProcessingOutput<>(exception, inputAudio);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - local path or url to input audio file
     */
    public AmplitudaProcessingOutput<String> processAudio(
            final String audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        InputAudio<String> inputAudio = new InputAudio<>(audio);

        try {
            // When audio is local file - process as file
            if(!URLUtil.isValidUrl(audio)) {
                inputAudio.setType(InputAudio.Type.PATH);
                return new AmplitudaProcessingOutput<>(
                        processFileJNI(new File(audio), inputAudio, compress, listener),
                        inputAudio
                );
            }
            // When audio is URL
            inputAudio.setType(InputAudio.Type.URL);

            // Save start time
            long startTime = System.currentTimeMillis();

            // Copy audio from url to local storage
            File tempAudio = fileManager.getUrlFile(audio);

            // Check for success copy operation from url to local tmp
            if(tempAudio == null) {
                return new AmplitudaProcessingOutput<>(
                        new InvalidAudioUrlException(),
                        inputAudio
                );
            }

            // Log operation time
            AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PREPARING, startTime);

            // Process local audio
            AmplitudaResultJNI result = processFileJNI(tempAudio, inputAudio, compress, listener);

            // Remove tmp file
            fileManager.deleteFile(tempAudio);

            return new AmplitudaProcessingOutput<>(result, inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return new AmplitudaProcessingOutput<>(exception, inputAudio);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - path to res/raw source file
     */
    public AmplitudaProcessingOutput<Integer> processAudio(
            final int audio,
            final Compress compress,
            final AmplitudaProgressListener listener
    ) {
        InputAudio<Integer> inputAudio = new InputAudio<>(audio, InputAudio.Type.RESOURCE);

        // Save start time
        long startTime = System.currentTimeMillis();

        // Copy raw to local file
        File tempAudio = fileManager.getRawFile(audio);

        // Check for success copy operation from res to local tmp
        if(tempAudio == null) {
            return new AmplitudaProcessingOutput<>(
                    new InvalidRawResourceException(),
                    inputAudio
            );
        }

        // Log operation time
        AmplitudaLogger.logOperationTime(AmplitudaLogger.OPERATION_PREPARING, startTime);

        try {
            // Process local raw file
            AmplitudaResultJNI result = processFileJNI(tempAudio, inputAudio, compress, listener);

            // Delete tmp
            fileManager.deleteFile(tempAudio);

            return new AmplitudaProcessingOutput<>(result, inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return new AmplitudaProcessingOutput<>(exception, inputAudio);
        }
    }

    public AmplitudaProcessingOutput<File> processAudio(final File audio) {
        return processAudio(audio, null, null);
    }

    public AmplitudaProcessingOutput<String> processAudio(final String audio) {
        return processAudio(audio, Compress.params(Compress.AVERAGE, 1), new AmplitudaProgressListener() {
            @Override
            public void onProgress(int percent) {
                System.out.println("On progress: " + percent);
            }
        });
    }

    public AmplitudaProcessingOutput<Integer> processAudio(final int audio) {
        return processAudio(audio, null, null);
    }

    /**
     * NDK part
     */
    static {
        System.loadLibrary("native-lib");
    }

//    native AmplitudaResultJNI amplitudesFromAudioJNI(String pathToAudio);
    native AmplitudaResultJNI amplitudesFromAudioJNI(
            String pathToAudio,
            int compressType,
            int framesPerSecond,
            AmplitudaProgressListener listener
    );

}
