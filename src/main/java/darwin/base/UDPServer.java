package darwin.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 张中海
 */
public abstract class UDPServer {
    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private NioEventLoopGroup loopGroup;
    private Channel channel;
    private volatile boolean started;
    private boolean broadCast;

    public void start() throws Exception {
        this.start(null, 0);
    }

    public void start(int port) throws Exception {
        this.start(null, port);
    }

    public void start(String host, int port) throws Exception {
        this.start(host, port, 0);
    }

    @SuppressWarnings("squid:S00112")
    public void start(String host, int port, int threadCount) throws Exception {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            Bootstrap bootstrap = new Bootstrap();
            loopGroup = new NioEventLoopGroup(threadCount);
            bootstrap.group(loopGroup)
                    .channel(NioDatagramChannel.class);
            MaxMessagesRecvByteBufAllocator recvByteBufAllocator = getRecvByteBufAllocator();
            if (null != recvByteBufAllocator) {
                bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, recvByteBufAllocator);
            }
            if (isBroadCast()) {
                bootstrap.option(ChannelOption.SO_BROADCAST, true);
            }
            bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                public void initChannel(NioDatagramChannel ch) {
                    UDPServer.this.initChannel(ch);
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                            super.channelReadComplete(ctx);
                        }
                    });
                }
            });
            try {
                if (host == null) {
                    channel = bootstrap.bind(port).sync().channel();
                } else {
                    channel = bootstrap.bind(host, port).sync().channel();
                }
                logger.info("UDPServer start success,port:{}", port);
                started = true;
            } catch (Exception e) {
                loopGroup.shutdownGracefully();
                throw e;
            }
        }
    }

    public boolean isBroadCast() {
        return broadCast;
    }

    public UDPServer setBroadCast(boolean broadCast) {
        this.broadCast = broadCast;
        return this;
    }

    public void stop() {
        if (!started) {
            return;
        }
        synchronized (this) {
            if (!started) {
                return;
            }
            channel.close();
            while (!loopGroup.isTerminated()) {
                loopGroup.shutdownGracefully();
            }
            logger.info("UDPServer stop success");
            started = false;
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isStarted() {
        return started;
    }

    public UDPServer setStarted(boolean started) {
        this.started = started;
        return this;
    }

    protected abstract void initChannel(NioDatagramChannel ch);

    protected MaxMessagesRecvByteBufAllocator getRecvByteBufAllocator() {
        return null;
    }


}
