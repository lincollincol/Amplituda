package linc.com.amplituda;

import android.content.Context;
import android.webkit.URLUtil;

import java.io.File;

import linc.com.amplituda.exceptions.*;
import linc.com.amplituda.exceptions.io.*;

public final class Amplituda {

    private static final String OPERATION_PROCESSING = "Processing";
    private static final String OPERATION_PREPARING = "Preparing";

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
    private AmplitudaResultJNI processFileJNI(final File audio) throws AmplitudaException {
        // Process audio
        if(!audio.exists()) {
            throw new FileNotFoundException();
        }

        if(!fileManager.isAudioFile(audio.getPath())) {
            throw new FileOpenException();
        }

        // Save start time
        long startTime = System.currentTimeMillis();

        // Process input audio
        AmplitudaResultJNI result = amplitudesFromAudioJNI(audio.getPath());

        // Log operation time
        AmplitudaLogger.logOperationTime(OPERATION_PROCESSING, startTime);
        return result;
    }

    public AmplitudaProcessingOutput<File> processAudio(final File audio) {
        InputAudio<File> inputAudio = new InputAudio<>(
                audio,
                fileManager.getAudioDuration(audio.getPath()),
                InputAudio.Type.FILE
        );
        try {
            return new AmplitudaProcessingOutput<>(processFileJNI(audio), inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return new AmplitudaProcessingOutput<>(exception, inputAudio);
        }
    }

    /**
     * Calculate amplitudes from file
     * @param audio - local path or url to input audio file
     */
    public AmplitudaProcessingOutput<String> processAudio(final String audio) {
        InputAudio<String> inputAudio = new InputAudio<>(audio);

        try {
            // When audio is local file - process as file
            if(!URLUtil.isValidUrl(audio)) {
                inputAudio.setType(InputAudio.Type.PATH);
                inputAudio.setDuration(fileManager.getAudioDuration(audio));
                return new AmplitudaProcessingOutput<>(
                        processFileJNI(new File(audio)),
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
            AmplitudaLogger.logOperationTime(OPERATION_PREPARING, startTime);

            // Process local audio
            AmplitudaResultJNI result = processFileJNI(tempAudio);

            // Save tmp file duration
            inputAudio.setDuration(fileManager.getAudioDuration(tempAudio.getPath()));

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
    public AmplitudaProcessingOutput<Integer> processAudio(final int audio) {
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
        AmplitudaLogger.logOperationTime(OPERATION_PREPARING, startTime);

        try {
            // Process local raw file
            AmplitudaResultJNI result = processFileJNI(tempAudio);

            // Save tmp resource file duration
            inputAudio.setDuration(fileManager.getAudioDuration(tempAudio.getPath()));

            // Delete tmp
            fileManager.deleteFile(tempAudio);

            return new AmplitudaProcessingOutput<>(result, inputAudio);
        } catch (AmplitudaException exception) {
            // Handle processing error
            return new AmplitudaProcessingOutput<>(exception, inputAudio);
        }
    }


    /**
     * NDK part
     */
    static {
        System.loadLibrary("native-lib");
    }

    native AmplitudaResultJNI amplitudesFromAudioJNI(String pathToAudio);

}
