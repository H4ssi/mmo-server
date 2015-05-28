package mmo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) {

		new ServerBootstrap()
				.group(new NioEventLoopGroup(), new NioEventLoopGroup())
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ch.pipeline().addLast(new HttpServerCodec())
								.addLast(new ChannelInboundHandlerAdapter() {
									@Override
									public void channelRead(
											final ChannelHandlerContext ctx,
											Object msg) throws Exception {
										System.out.println(msg);
										if (msg instanceof HttpRequest) {
											HttpResponse res = new DefaultHttpResponse(
													HttpVersion.HTTP_1_1,
													HttpResponseStatus.OK);

											res.headers()
													.set(HttpHeaders.Names.CONTENT_TYPE,
															"text/html; charset=utf-8");
											res.headers()
													.set(HttpHeaders.Names.TRANSFER_ENCODING,
															HttpHeaders.Values.CHUNKED);

											ctx.writeAndFlush(res);

											final ByteBuf b = ctx.alloc()
													.buffer();
											b.writeBytes("hello\n".getBytes());

											final ByteBuf i = ctx.alloc()
													.buffer();
											i.writeBytes("<!DOCTYPE html><html><body><pre>\n"
													.getBytes());
											ctx.writeAndFlush(new DefaultHttpContent(
													i));

											HashedWheelTimer t = new HashedWheelTimer();

											t.newTimeout(new TimerTask() {

												@Override
												public void run(Timeout timeout)
														throws Exception {
													b.resetReaderIndex();
													b.retain();
													ctx.writeAndFlush(new DefaultHttpContent(
															b));
													System.out.println(1);
												}
											}, 1, TimeUnit.SECONDS);
											t.newTimeout(new TimerTask() {

												@Override
												public void run(Timeout timeout)
														throws Exception {
													b.resetReaderIndex();
													b.retain();
													ctx.writeAndFlush(new DefaultHttpContent(
															b));
													System.out.println(2);
												}
											}, 2, TimeUnit.SECONDS);
											t.newTimeout(new TimerTask() {

												@Override
												public void run(Timeout timeout)
														throws Exception {
													b.resetReaderIndex();
													b.retain();
													ctx.writeAndFlush(new DefaultHttpContent(
															b));
													System.out.println(3);
												}
											}, 3, TimeUnit.SECONDS);
											t.newTimeout(new TimerTask() {

												@Override
												public void run(Timeout timeout)
														throws Exception {
													b.resetReaderIndex();
													b.retain();
													ctx.writeAndFlush(
															new DefaultLastHttpContent())
															.addListener(
																	new ChannelFutureListener() {

																		@Override
																		public void operationComplete(
																				ChannelFuture future)
																				throws Exception {
																			ctx.close();
																			b.release();
																			System.out
																					.println("closed");
																		}
																	});
													System.out.println(4);
												}
											}, 4, TimeUnit.SECONDS);
										} else if (msg instanceof LastHttpContent) {
											System.out
													.println("client end of data");
										} else if (msg instanceof HttpContent) {
											ByteBuf b = ctx.alloc().buffer();
											b.resetWriterIndex();
											b.writeBytes("pong".getBytes());
											ctx.writeAndFlush(new DefaultHttpContent(
													b));
										}
									}
								});
					};
				}).option(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.TCP_NODELAY, true).bind(8080);
	}

}
