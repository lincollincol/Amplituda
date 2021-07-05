package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.CODEC_NOT_FOUND_PROC_CODE;

public final class CodecNotFoundException extends AmplitudaProcessingException {
    public CodecNotFoundException() {
        super("Failed to find codec!", CODEC_NOT_FOUND_PROC_CODE);
    }
}
