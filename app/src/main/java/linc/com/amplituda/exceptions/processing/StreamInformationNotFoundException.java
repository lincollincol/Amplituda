package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.STREAM_INFO_NOT_FOUND_PROC_CODE;

public final class StreamInformationNotFoundException extends AmplitudaProcessingException {
    public StreamInformationNotFoundException() {
        super("Could not find stream information!", STREAM_INFO_NOT_FOUND_PROC_CODE);
    }
}
