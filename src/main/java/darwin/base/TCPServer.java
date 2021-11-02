package darwin.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 */
public abstract class TCPServer {
    private EventLoopGroup loopGroup;

    private volatile boolean started;

    public boolean isStarted() {
        return started;
    }

    public void start(int port) throws Exception {
        this.start(port, null);

    }

    @SuppressWarnings("squid:S00112")
    public void start(int port, NioEventLoopGroup loopGroup) throws Exception {
        if (started) {
            return;
        }

        synchronized (this) {
            if (started) {
                return;
            }
            //配置 NIO线程组
            this.loopGroup = loopGroup != null ? loopGroup : new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(this.loopGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            TCPServer.this.initChannel(ch);
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                    super.channelReadComplete(ctx);
                                }
                            });
                        }
                    });
            b.bind(port).sync();
            started = true;
        }
    }

    public void stop() {
        if (!started) {
            return;
        }

        synchronized (this) {
            if (!started) {
                return;
            }
            if (loopGroup != null) {
                loopGroup.shutdownGracefully();
                loopGroup = null;
            }
            started = false;
        }
    }

    protected abstract void initChannel(SocketChannel ch);
}
