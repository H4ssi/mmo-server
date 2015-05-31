package mmo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

import javax.inject.Inject;
import javax.inject.Provider;

public class Server {
    private final Provider<Handler> handlerProvider;

    @Inject
    public Server(Provider<Handler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    public void run() {
        new ServerBootstrap()
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline().addLast(new HttpServerCodec())
                                .addLast(handlerProvider.get());
                    }

                    ;
                }).option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.TCP_NODELAY, true).bind(8080);
    }
}