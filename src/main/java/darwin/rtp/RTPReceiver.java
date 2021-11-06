package darwin.rtp;

import darwin.base.UDPServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangzheng
 * @since 2021/11/4 20:40
 */
public class RTPReceiver extends UDPServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RTPReceiver.class);

    @Override
    protected void initChannel(NioDatagramChannel ch) {
        ch.pipeline().addLast(new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
                ByteBuf buf = msg.content();
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                RTPpacket rtPpacket = new RTPpacket(bytes);
                RtpParser.H264PacketRecombine(rtPpacket.getPayload(), rtPpacket.getPayload().length);
                LOGGER.debug("receive raw data -> {}", rtPpacket);
                LOGGER.debug("NAL unit count -> {}", RtpParser.frames.size());
//                LOGGER.debug("receive data-> {}", rtPpacket);
            }
        });
    }
}
