package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.CODEC_PARAMETERS_ERR;

public final class CodecParametersException extends AmplitudaProcessingException {
    public CodecParametersException() {
        super("Failed to copy codec parameters to decoder context!", CODEC_PARAMETERS_ERR);
    }
}
