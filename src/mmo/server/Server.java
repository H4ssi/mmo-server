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
    private final GameLoop gameLoop;
    private NioEventLoopGroup parentGroup;
    private NioEventLoopGroup childGroup;

    @Inject
    public Server(Provider<Handler> handlerProvider, HashedWheelTimer timer,
                  GameLoop gameLoop) {
        this.handlerProvider = handlerProvider;
        this.timer = timer;
        this.gameLoop = gameLoop;
    }

    public void run(String host, int port) {
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
                .bind(host, port);
    }

    public void shutdown() throws InterruptedException {
        timer.stop();
        Future<?> parentShutdown = parentGroup.shutdownGracefully();
        Future<?> childShutdown = childGroup.shutdownGracefully();
        Future<?> gameLoopShutdown = gameLoop.shutdownGracefully();
        parentShutdown.await();
        childShutdown.await();
        gameLoopShutdown.await();
    }
}