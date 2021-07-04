package linc.com.amplituda.exceptions.allocation;

import static linc.com.amplituda.ErrorCode.ALLOC_PACKET_ERR;

public final class PacketAllocationException extends AmplitudaAllocationException {
    public PacketAllocationException() {
        super("Could not allocate packet!", ALLOC_PACKET_ERR);
    }
}
