package linc.com.amplituda.exceptions.processing;

import linc.com.amplituda.exceptions.io.AmplitudaIOException;

import static linc.com.amplituda.ErrorCode.PACKET_SUBMITTING_ERR;

public final class PacketSubmittingException extends AmplitudaProcessingException {
    public PacketSubmittingException() {
        super("Error submitting a packet for decoding!", PACKET_SUBMITTING_ERR);
    }
}
