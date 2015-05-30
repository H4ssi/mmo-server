package mmo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class Main {

	public static void main(String[] args) {

		new ServerBootstrap()
				.group(new NioEventLoopGroup(), new NioEventLoopGroup())
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ch.pipeline().addLast(new HttpServerCodec())
								.addLast(new Handler());
					};
				}).option(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.TCP_NODELAY, true).bind(8080);
	}

}
