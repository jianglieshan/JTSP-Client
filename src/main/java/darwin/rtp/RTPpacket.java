package darwin.rtp;

/**
 * @author jiangzheng
 * @since 2021/11/4 20:57
 */
public class RTPpacket {

    /**
     * header size
     */
    private static final int HEADER_SIZE = 12;

    /**
     * rtp header
     */
    private int version;
    private int padding;
    private int extension;
    private int cc;
    private int marker;
    private int payloadType;
    private int sequenceNumber;
    private int timeStamp;
    private int ssrc;

    private byte[] header;

    private byte[] payload;

    private int f;
    private int nri;
    private int type;

    private int s;
    private int e;
    private int r;
    private int fuType;


    public RTPpacket(byte[] packet) {
//fill default fields:
        version = 2;
        padding = 0;
        extension = 0;
        cc = 0;
        marker = 0;
        ssrc = 0;

        int packetSize = packet.length;
        //check if total packet size is lower than the header size
        if (packetSize >= HEADER_SIZE) {
            //get the header bitsream:
            header = new byte[HEADER_SIZE];
            for (int i = 0; i < HEADER_SIZE; i++) {
                header[i] = packet[i];
            }

            //get the payload bitstream:
            int payloadSize = packetSize - HEADER_SIZE;
            payload = new byte[payloadSize];
            for (int i = HEADER_SIZE; i < packetSize; i++) {
                payload[i - HEADER_SIZE] = packet[i];
            }

            //interpret the changing fields of the header:
            version = (header[0] & 0xFF) >>> 6;
            payloadType = header[1] & 0x7F;
            sequenceNumber = (header[3] & 0xFF) + ((header[2] & 0xFF) << 8);
            timeStamp = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24);

            byte naluHeader = payload[0];
            f = naluHeader >> 7;
            nri = (naluHeader >> 5) & 0x3;
            type = naluHeader & 0x1F;
            if (type == 28) {
                byte fuHeader = payload[1];
                s = fuHeader >> 7;
                e = (fuHeader >> 6) & 0x01;
                r = (fuHeader >> 5) & 0x01;
                fuType = fuHeader & 0x1F;

            }

        }
    }

    @Override
    public String toString() {
        return String.format("f->%d nri->%d type->%d s->%d e->%d r->%d fuType->%d", f, nri, type, s, e, r, fuType);
    }
}
