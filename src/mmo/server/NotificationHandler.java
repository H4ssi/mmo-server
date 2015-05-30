package mmo.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

import mmo.server.Handler.HandlerContext;

public class NotificationHandler extends AbstractHandler {

	public NotificationHandler(HandlerContext handlerContext) {
		super(handlerContext);
	}

	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest) {
			HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
					HttpResponseStatus.OK);

			// this is needed for browsers to render content as soon as it is
			// received
			HttpHeaders.setHeader(res, HttpHeaders.Names.CONTENT_TYPE,
					"text/html; charset=utf-8");
			HttpHeaders.setKeepAlive(res, false);
			HttpHeaders.setTransferEncodingChunked(res);

			ctx.writeAndFlush(res);

			final ByteBuf b = ctx.alloc().buffer();
			b.writeBytes("hello\n".getBytes());

			final ByteBuf i = ctx.alloc().buffer();
			i.writeBytes("<!DOCTYPE html><html><body><pre>\n".getBytes());
			ctx.writeAndFlush(new DefaultHttpContent(i));

			HashedWheelTimer t = new HashedWheelTimer();

			t.newTimeout(new TimerTask() {

				@Override
				public void run(Timeout timeout) throws Exception {
					b.resetReaderIndex();
					b.retain();
					ctx.writeAndFlush(new DefaultHttpContent(b));
					System.out.println(1);
				}
			}, 1, TimeUnit.SECONDS);
			t.newTimeout(new TimerTask() {

				@Override
				public void run(Timeout timeout) throws Exception {
					b.resetReaderIndex();
					b.retain();
					ctx.writeAndFlush(new DefaultHttpContent(b));
					System.out.println(2);
				}
			}, 2, TimeUnit.SECONDS);
			t.newTimeout(new TimerTask() {

				@Override
				public void run(Timeout timeout) throws Exception {
					b.resetReaderIndex();
					b.retain();
					ctx.writeAndFlush(new DefaultHttpContent(b));
					System.out.println(3);
				}
			}, 3, TimeUnit.SECONDS);
			t.newTimeout(new TimerTask() {

				@Override
				public void run(Timeout timeout) throws Exception {
					b.resetReaderIndex();
					b.retain();
					ctx.writeAndFlush(new DefaultLastHttpContent())
							.addListener(new ChannelFutureListener() {

								@Override
								public void operationComplete(
										ChannelFuture future) throws Exception {
									ctx.close();
									b.release();
									getHandlerContext().unregister();
									System.out.println("closed");
								}
							});
					System.out.println(4);
				}
			}, 4, TimeUnit.SECONDS);
		} else if (msg instanceof LastHttpContent) {
			System.out.println("client end of data");
		} else if (msg instanceof HttpContent) {
			ByteBuf b = ctx.alloc().buffer();
			b.resetWriterIndex();
			b.writeBytes("pong".getBytes());
			ctx.writeAndFlush(new DefaultHttpContent(b));
		}
	}
}
