package darwin.base;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author jiangzheng
 * @since 2021/10/30 15:32
 */
public abstract class TcpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);


    private final String host;
    private final int port;
    private final int threadCount;
    private Channel channel;
    private Thread sendThread;

    private BlockingQueue<Object> toSendQueue = new LinkedBlockingQueue<>();

    public TcpClient(String host, int port, int threadCount) {
        this.host = host;
        this.port = port;
        this.threadCount = threadCount;
    }

    public TcpClient(String host, int port) {
        this(host, port, 0);
    }

    public Channel getChannel() {
        return channel;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup(threadCount);

        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        LOGGER.info("正在连接中...");
                        TcpClient.this.initChannel(ch);
                        TcpClient.this.channel = ch;
                    }
                });
        final ChannelFuture future = b.connect(host, port).syncUninterruptibly();

        this.sendThread = new Thread(() -> {
            while (true) {
                doSend();
            }
        });
        this.sendThread.start();

//        Channel futureChannel = future.channel();
//        futureChannel.closeFuture().syncUninterruptibly();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture arg0) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("连接服务器成功");
                } else {
                    LOGGER.info("连接服务器失败");
                    future.cause().printStackTrace();
                    group.shutdownGracefully();
                }
            }
        });
    }

    protected void doSend() {
        Object take = null;
        try {
            take = toSendQueue.take();
            channel.writeAndFlush(take);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 业务方自主添加channel
     *
     * @param channel socket channel
     */
    protected abstract void initChannel(SocketChannel channel);

    public void send(Object data) {
        toSendQueue.add(data);
    }
}
