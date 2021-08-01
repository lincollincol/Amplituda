package linc.com.amplituda.exceptions.io;

import static linc.com.amplituda.ErrorCode.EXTENDED_PROCESSING_DISABLED_IO_CODE;

public class ExtendedProcessingDisabledException extends AmplitudaIOException {
    public ExtendedProcessingDisabledException() {
        super("You are trying to process audio from res/raw or url with disabled extended processing.\nPlease call `enableExtendedProcessing(final Context context)` while building Amplituda", EXTENDED_PROCESSING_DISABLED_IO_CODE);
    }
}
