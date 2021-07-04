package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.NOT_FOUND_STREAM_INFO;

public final class StreamInformationNotFoundException extends AmplitudaProcessingException {
    public StreamInformationNotFoundException() {
        super("Could not find stream information!", NOT_FOUND_STREAM_INFO);
    }
}
