package darwin.rtp;

/**
 * @author jiangzheng
 * @since 2021/11/4 20:57
 * <p>
 * 0 1 2 3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * ++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- ++-+-+-+-+-+-+-+
 * |V=2|P|X| 抄送 |M| PT | 序号 |
 * ++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- ++-+-+-+-+-+-+-+
 * | 时间戳 |
 * ++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- ++-+-+-+-+-+-+-+
 * | 同步源 (SSRC) 标识符 |
 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+= +=+=+=+=+=+=+=+
 * | 贡献来源 (CSRC) 标识符 |
 * | .... |
 * ++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- ++-+-+-+-+-+-+-+
 * <p>
 * https://datatracker.ietf.org/doc/html/rfc6184#section-5.1
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

    /**
     * 如果时10，表示时一帧数据的开头部分；
     * 如果是00，表示一帧数据的中间部分，有可能多个00的分片；
     * 如果是01，表示一帧数据的结尾。
     */
    private int start;

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
            f = (naluHeader & 0x80) >> 7;
            nri = (naluHeader & 0x60) >> 5;
            type = naluHeader & 0x1F;
            switch (type) {
                case 28: {
                    byte fuHeader = payload[1];
                    start = (fuHeader & 0xC0) >> 6;
                    r = (fuHeader & 0x1F);
                    fuType = fuHeader & 0x1F;
                }
                default:

            }

        }
    }

    private int r;
    private int fuType;

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return String.format("f->%d nri->%d type->%d\r\n start->%d r->%d fuType->%d\r\n timeStamp->%d seq->%d", f, nri, type, start, r, fuType, timeStamp, sequenceNumber);
    }
}
