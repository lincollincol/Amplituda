package linc.com.amplituda.exceptions.allocation;

import static linc.com.amplituda.ErrorCode.ALLOC_CODEC_CTX_ERR;

public final class CodecContextAllocationException extends AmplitudaAllocationException {
    public CodecContextAllocationException() {
        super("Failed to allocate the codec context!", ALLOC_CODEC_CTX_ERR);
    }
}
