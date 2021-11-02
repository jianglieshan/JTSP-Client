package darwin.channel;

import darwin.rtsp.RtspClient;
import darwin.rtsp.RtspProtocolConst;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * @author jiangzheng
 * @since 2021/10/31 22:23
 */
public class RtspInboundChannel extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger("RtspInboundChannel");

    private final StringBuilder cache = new StringBuilder();

    private final WeakReference<RtspClient> clientReference;

    public RtspInboundChannel(WeakReference<RtspClient> clientReference) {
        this.clientReference = clientReference;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.debug((String) msg);
        cache.append((String) msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        String allContent = cache.toString();
        if (allContent.startsWith(RtspProtocolConst.SUCCESS_RESPONSE)) {
            clientReference.get().parseResponse(allContent);
        } else {
            LOGGER.error("server error response -> {}", allContent);
        }
        cache.setLength(0);
        super.channelReadComplete(ctx);
    }
}
