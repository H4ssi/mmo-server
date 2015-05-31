package mmo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

public class Server {
    private final Provider<Handler> handlerProvider;
    private final HashedWheelTimer timer;
    private NioEventLoopGroup parentGroup;
    private NioEventLoopGroup childGroup;


    @Inject
    public Server(Provider<Handler> handlerProvider, HashedWheelTimer timer) {
        this.handlerProvider = handlerProvider;
        this.timer = timer;
    }

    public void run() {
        parentGroup = new NioEventLoopGroup();
        childGroup = new NioEventLoopGroup();
        new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(handlerProvider.get());
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .bind(8080);
    }

    public void shutdown() throws InterruptedException {
        timer.stop();
        Future<?> parentShutdown = parentGroup.shutdownGracefully();
        Future<?> childShutdown = childGroup.shutdownGracefully();
        parentShutdown.await();
        childShutdown.await();
    }
}