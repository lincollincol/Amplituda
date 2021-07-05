package linc.com.amplituda.exceptions.processing;

import static linc.com.amplituda.ErrorCode.PACKET_SUBMITTING_PROC_CODE;

public final class PacketSubmittingException extends AmplitudaProcessingException {
    public PacketSubmittingException() {
        super("Error submitting a packet for decoding!", PACKET_SUBMITTING_PROC_CODE);
    }
}
