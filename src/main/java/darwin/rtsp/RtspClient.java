package darwin.rtsp;

import darwin.base.TcpClient;
import darwin.base.UDPServer;
import darwin.channel.RtspInboundChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author jiangzheng
 * @since 2021/10/30 15:10
 */
public class RtspClient implements RtspDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(RtspClient.class);
    private final String address;
    private final String ip;
    private final int port;
    private int cSeq;
    private TcpClient tcpClient;
    private UDPServer rtpReceiver;
    private UDPServer rtcpReceiver;
    private Map<Integer, RtspDirectiveEnum> statusMap = new HashMap<>();
    private volatile String sessionId;

    public RtspClient(String ip, int port, String address) {
        this.address = address;
        this.ip = ip;
        this.port = port;
        this.cSeq = 0;
    }

    public void start() {
        tcpClient = new TcpClient(ip, port) {
            @Override
            protected void initChannel(SocketChannel channel) {
                channel.pipeline()
                        .addLast(new StringEncoder())
                        .addLast(new StringDecoder())
                        .addLast(new RtspInboundChannel(new WeakReference<>(RtspClient.this)));
            }
        };
        tcpClient.start();
    }

    @Override
    public void options() {
        sendRequest(RtspDirectiveEnum.OPTIONS, null);
    }

    @Override
    public void describe() {
        sendRequest(RtspDirectiveEnum.DESCRIBE, null);
    }

    @Override
    public void setup() {
        sendRequest(RtspDirectiveEnum.SETUP, () -> String.format("Transport: RTP/UDP; client_port=%d-%d", RtspProtocolConst.RTP_RCV_PORT, RtspProtocolConst.RTP_RCV_PORT + 1));
    }

    @Override
    public void play() {
        sendRequest(RtspDirectiveEnum.PLAY, null);
    }

    @Override
    public void pause() {

    }

    @Override
    public void record() {

    }

    @Override
    public void announce() {

    }

    @Override
    public void teardown() {
        sendRequest(RtspDirectiveEnum.TEARDOWN, null);
    }

    @Override
    public void getParameter() {

    }

    @Override
    public void setParameter() {

    }

    @Override
    public void redirect() {
    }

    private void sendRequest(RtspDirectiveEnum directive, Supplier<String> supplier) {
        this.cSeq += 1;
        statusMap.put(this.cSeq, directive);
        StringBuilder builder = new StringBuilder();
        builder.append(directive.name())
                .append(StringUtil.SPACE)
                .append(this.address)
                .append(StringUtil.SPACE)
                .append(RtspProtocolConst.VERSION)
                .append(StringUtil.NEWLINE)
                .append(RtspProtocolConst.C_SEQ)
                .append(this.cSeq)
                .append(StringUtil.NEWLINE);
        if (sessionId != null) {
            builder.append("Session: ")
                    .append(sessionId)
                    .append(StringUtil.NEWLINE);
        }
        if (supplier != null) {
            builder.append(supplier.get());
            builder.append(StringUtil.NEWLINE);
        }
        LOGGER.debug("send content ->{}", builder);
        this.tcpClient.send(builder.toString());
    }

    public void parseResponse(String content) {
        String[] split = content.split("\r\n");
        String seqLine = split[1];
        int seq = Integer.parseInt(seqLine.replace(RtspProtocolConst.C_SEQ, ""));
        RtspDirectiveEnum directiveEnum = statusMap.remove(seq);
        directiveEnum.trigger(content, this);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void startReceiveRTPPacket(int rtpPort, int rtcpPort) {
        rtpReceiver = startUdpServer(rtpPort);
        rtcpReceiver = startUdpServer(rtcpPort);
    }

    private UDPServer startUdpServer(int port) {
        UDPServer server = new UDPServer() {
            @Override
            protected void initChannel(NioDatagramChannel ch) {
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        LOGGER.debug("receive -> {}", msg);
                        super.channelRead(ctx, msg);
                    }
                });
            }
        };
        try {
            server.start(port);
        } catch (Exception exception) {
            LOGGER.error("rtp receiver start errror ", exception);
        }
        return server;
    }
}
