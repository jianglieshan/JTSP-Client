package darwin.rtp;

/**
 * @author jiangzheng
 * @since 2021/11/4 20:42
 * <p>
 * 0             1               2               3               4
 * 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |V=2|P|X|  CC   |M|     PT      |       sequence number         |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                           timestamp                           |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |           synchronization source (SSRC) identifier            |
 * +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+-+
 * |            contributing source (CSRC) identifiers             |
 * |                             ....                              |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type   Packet      Type name
 * ---------------------------------------------------------
 * 0      undefined                                    -
 * 1-23   NAL unit    Single NAL unit packet per H.264
 * 24     STAP-A     Single-time aggregation packet
 * 25     STAP-B     Single-time aggregation packet
 * 26     MTAP16    Multi-time aggregation packet
 * 27     MTAP24    Multi-time aggregation packet
 * 28     FU-A      Fragmentation unit
 * 29     FU-B      Fragmentation unit
 * 30-31  undefined
 */
public class RtpParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtpParser.class);

    private static final int NAL_UNIT_TYPE_STAP_A = 24;

    private static final int NAL_UNIT_TYPE_STAP_B = 25;

    private static final int NAL_UNIT_TYPE_MTAP16 = 26;
    private static final int NAL_UNIT_TYPE_MTAP24 = 27;
    private static final int NAL_UNIT_TYPE_FU_A = 28;
    private static final int NAL_UNIT_TYPE_FU_B = 29;
    public static List<byte[]> frames = new ArrayList<>();
    private static byte[] NALUnit = new byte[1024 * 1024];
    private static int tmpLen = 0;


    public static void H264PacketRecombine(byte[] data, int length) {

        int nalType = data[0] & 0x1F;

        switch (nalType) {
            //Single-timeaggregation packet
            case NAL_UNIT_TYPE_STAP_A:
                break;

            //Single-timeaggregation packet
            case NAL_UNIT_TYPE_STAP_B:
                break;

            //Multi-time aggregationpacket
            case NAL_UNIT_TYPE_MTAP16:
                break;

            //Multi-time aggregationpacket
            case NAL_UNIT_TYPE_MTAP24:
                break;

            //Fragmentationunit
            case NAL_UNIT_TYPE_FU_A:
                int packFlag = data[1] & 0xC0;
                int body = length - 2;
                switch (packFlag) {
                    //NAL Unit start packet
                    case 0x80://一帧的开头
                        if (RtpParser.tmpLen != 0) {
                            frames.add(Arrays.copyOfRange(NALUnit, 0, tmpLen));
                            LOGGER.info("rtp final length-> {}", RtpParser.tmpLen);
                            RtpParser.tmpLen = 0;
                        }

                        NALUnit[4] = (byte) ((data[0] & 0xE0) | (data[1] & 0x1F));
                        System.arraycopy(data, 2, NALUnit, 5, body);
                        RtpParser.tmpLen = body + 5;
                        break;
                    //NAL Unit middle packet
                    case 0x00:
                        System.arraycopy(data, 2, NALUnit, RtpParser.tmpLen, body);
                        RtpParser.tmpLen += body;
                        break;
                    //NAL Unit end packet
                    case 0x40:
                        System.arraycopy(data, 2, NALUnit, RtpParser.tmpLen, body);
                        RtpParser.tmpLen += body;
                        frames.add(Arrays.copyOfRange(NALUnit, 0, tmpLen));//开始解码
                        tmpLen = 0;
                        break;
                    default: {
                        break;
                    }
                }
                break;

            //Fragmentationunit
            case NAL_UNIT_TYPE_FU_B:
                break;

            //Single NAL unit per packet
            default:
//                if (SquareApplication.getInstance().getH264Stream() != null) {
//
//                    System.arraycopy(data, 0, BaseNALUnit, 4, length);
//                    SquareApplication.getInstance().getH264Stream().decodeH264Stream(Arrays.copyOfRange(BaseNALUnit, 0, (4 + length)));//开始解码
//
//                    tmpLen = 0;
//                }
                break;
        }

    }

}
