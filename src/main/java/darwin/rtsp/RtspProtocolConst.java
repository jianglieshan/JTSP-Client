package darwin.rtsp;

import io.netty.util.internal.StringUtil;

/**
 * @author jiangzheng
 * @since 2021/10/31 21:57
 */
public class RtspProtocolConst {
    public static final String VERSION = "RTSP/1.0";
    public static final String C_SEQ = "CSeq: ";
    public static final String OPTIONS_CONTENT = "Require: implicit-play" + StringUtil.NEWLINE + "Proxy-Require: gzipped-messages";
    public static final String SUCCESS_RESPONSE = "RTSP/1.0 200 OK";
    public static final int RTP_RCV_PORT = 25002;

    private RtspProtocolConst() {

    }

}
