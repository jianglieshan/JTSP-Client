package darwin;

import darwin.base.TCPServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangzheng
 * @since 2021/10/31 17:32
 */
public class TestTcpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestTcpServer.class);

    public static void main(String[] args) throws Exception {
        TCPServer server = new TCPServer() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new StringEncoder());
                ch.pipeline().addLast(new LineEncoder());
                ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                        LOGGER.info(msg);
                        Thread.sleep(1000);
                        ctx.writeAndFlush("server test\n");
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ctx.writeAndFlush("hello\n");
                        super.channelActive(ctx);
                    }
                });
            }
        };
        server.start(7550);
    }
}