package linc.com.amplituda.exceptions.allocation;

import static linc.com.amplituda.ErrorCode.PACKET_ALLOC_CODE;

public final class PacketAllocationException extends AmplitudaAllocationException {
    public PacketAllocationException() {
        super("Could not allocate packet!", PACKET_ALLOC_CODE);
    }
}
