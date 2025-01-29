package com.linc.amplituda;

import java.util.Arrays;
import java.util.LinkedHashSet;

import com.linc.amplituda.exceptions.AmplitudaException;
import com.linc.amplituda.exceptions.allocation.CodecContextAllocationException;
import com.linc.amplituda.exceptions.allocation.FrameAllocationException;
import com.linc.amplituda.exceptions.allocation.PacketAllocationException;
import com.linc.amplituda.exceptions.io.FileNotFoundException;
import com.linc.amplituda.exceptions.io.FileOpenException;
import com.linc.amplituda.exceptions.io.InvalidRawResourceException;
import com.linc.amplituda.exceptions.io.NoInputFileException;
import com.linc.amplituda.exceptions.processing.CodecNotFoundException;
import com.linc.amplituda.exceptions.processing.CodecOpenException;
import com.linc.amplituda.exceptions.processing.CodecParametersException;
import com.linc.amplituda.exceptions.processing.DecodingException;
import com.linc.amplituda.exceptions.processing.InvalidParameterFlagException;
import com.linc.amplituda.exceptions.processing.PacketSubmittingException;
import com.linc.amplituda.exceptions.processing.SampleOutOfBoundsException;
import com.linc.amplituda.exceptions.processing.SecondOutOfBoundsException;
import com.linc.amplituda.exceptions.processing.StreamInformationNotFoundException;
import com.linc.amplituda.exceptions.processing.StreamNotFoundException;
import com.linc.amplituda.exceptions.processing.UnsupportedSampleFormatException;

import static com.linc.amplituda.ErrorCode.CODEC_CONTEXT_ALLOC_CODE;
import static com.linc.amplituda.ErrorCode.CODEC_NOT_FOUND_PROC_CODE;
import static com.linc.amplituda.ErrorCode.CODEC_OPEN_PROC_CODE;
import static com.linc.amplituda.ErrorCode.CODEC_PARAMETERS_COPY_PROC_CODE;
import static com.linc.amplituda.ErrorCode.DECODING_PROC_CODE;
import static com.linc.amplituda.ErrorCode.FILE_NOT_FOUND_IO_CODE;
import static com.linc.amplituda.ErrorCode.FILE_OPEN_IO_CODE;
import static com.linc.amplituda.ErrorCode.FRAME_ALLOC_CODE;
import static com.linc.amplituda.ErrorCode.INVALID_PARAMETER_FLAG_PROC_CODE;
import static com.linc.amplituda.ErrorCode.INVALID_RAW_RESOURCE_IO_CODE;
import static com.linc.amplituda.ErrorCode.NO_INPUT_FILE_IO_CODE;
import static com.linc.amplituda.ErrorCode.PACKET_ALLOC_CODE;
import static com.linc.amplituda.ErrorCode.PACKET_SUBMITTING_PROC_CODE;
import static com.linc.amplituda.ErrorCode.SAMPLE_OUT_OF_BOUNDS_PROC_CODE;
import static com.linc.amplituda.ErrorCode.SECOND_OUT_OF_BOUNDS_PROC_CODE;
import static com.linc.amplituda.ErrorCode.STREAM_INFO_NOT_FOUND_PROC_CODE;
import static com.linc.amplituda.ErrorCode.STREAM_NOT_FOUND_PROC_CODE;
import static com.linc.amplituda.ErrorCode.UNSUPPORTED_SAMPLE_FMT_PROC_CODE;

final class AmplitudaResultJNI {

    private double duration;
    private String amplitudes;
    private String errors;

    /**
     * Get audio duration
     */
    long getDurationMillis() {
        return (long) (duration * 1000);
    }

    /**
     * Get ndk processing result
     */
    String getAmplitudes() {
        return amplitudes;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setAmplitudes(String amplitudes) {
        this.amplitudes = amplitudes;
    }

    /**
     * Get ndk exceptions according to codes
     */
    LinkedHashSet<AmplitudaException> getErrors() {
        if(errors == null || errors.isEmpty()) {
            return new LinkedHashSet<>();
        }
        LinkedHashSet<AmplitudaException> errors = new LinkedHashSet<>();
        for(String error : this.errors.split(" ")) {
            if(error.isEmpty())
                continue;
            errors.add(getExceptionFromCode(Integer.parseInt(error)));
        }
        return errors;
    }

    /**
     * Get exception according to code
     * @param code - exception code. All codes => ErrorCode.java
     * @return exception from code. Return global AmplitudaException when code is unknown
     */
    private AmplitudaException getExceptionFromCode(final int code) {
        switch (code) {
            case FRAME_ALLOC_CODE:                 return new FrameAllocationException();
            case PACKET_ALLOC_CODE:                return new PacketAllocationException();
            case CODEC_CONTEXT_ALLOC_CODE:         return new CodecContextAllocationException();
            case FILE_OPEN_IO_CODE:                return new FileOpenException();
            case CODEC_NOT_FOUND_PROC_CODE:        return new CodecNotFoundException();
            case STREAM_NOT_FOUND_PROC_CODE:       return new StreamNotFoundException();
            case STREAM_INFO_NOT_FOUND_PROC_CODE:  return new StreamInformationNotFoundException();
            case CODEC_PARAMETERS_COPY_PROC_CODE:  return new CodecParametersException();
            case PACKET_SUBMITTING_PROC_CODE:      return new PacketSubmittingException();
            case CODEC_OPEN_PROC_CODE:             return new CodecOpenException();
            case UNSUPPORTED_SAMPLE_FMT_PROC_CODE: return new UnsupportedSampleFormatException();
            case DECODING_PROC_CODE:               return new DecodingException();
            case SAMPLE_OUT_OF_BOUNDS_PROC_CODE:   return new SampleOutOfBoundsException();
            default:                               return new AmplitudaException();
        }
    }

}
