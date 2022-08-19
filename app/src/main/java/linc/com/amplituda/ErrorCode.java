package linc.com.amplituda;

public final class ErrorCode {

    public static final int AMPLITUDA_EXCEPTION = 1;

    /**
     * Alloc code
     */

    public static final int FRAME_ALLOC_CODE = 10;
    public static final int PACKET_ALLOC_CODE = 11;
    public static final int CODEC_CONTEXT_ALLOC_CODE = 12;

    /**
     * IO code
     */

    public static final int FILE_OPEN_IO_CODE = 20;
    public static final int FILE_NOT_FOUND_IO_CODE = 21;
    public static final int INVALID_RAW_RESOURCE_IO_CODE = 22;
    public static final int NO_INPUT_FILE_IO_CODE = 23;
    public static final int INVALID_AUDIO_URL_IO_CODE = 24;
    public static final int EXTENDED_PROCESSING_DISABLED_IO_CODE = 25;
    public static final int INVALID_AUDIO_INPUT_STREAM_IO_CODE = 26;
    public static final int INVALID_AUDIO_BYTE_ARRAY_IO_CODE = 27;

    /**
     * Processing code
     */

    public static final int CODEC_NOT_FOUND_PROC_CODE = 30;
    public static final int STREAM_NOT_FOUND_PROC_CODE = 31;
    public static final int STREAM_INFO_NOT_FOUND_PROC_CODE = 32;
    public static final int CODEC_PARAMETERS_COPY_PROC_CODE = 33;
    public static final int PACKET_SUBMITTING_PROC_CODE = 34;
    public static final int CODEC_OPEN_PROC_CODE = 35;
    public static final int UNSUPPORTED_SAMPLE_FMT_PROC_CODE = 36;
    public static final int DECODING_PROC_CODE = 37;
    public static final int INVALID_PARAMETER_FLAG_PROC_CODE = 38;
    public static final int SECOND_OUT_OF_BOUNDS_PROC_CODE = 39;
    public static final int SAMPLE_OUT_OF_BOUNDS_PROC_CODE = 40;

}
