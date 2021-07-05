package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.CODEC_PARAMETERS_COPY_PROC_CODE;

public final class CodecParametersException extends AmplitudaProcessingException {
    public CodecParametersException() {
        super("Failed to copy codec parameters to decoder context!", CODEC_PARAMETERS_COPY_PROC_CODE);
    }
}
