package mmo.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;

public class Handler extends ChannelInboundHandlerAdapter {
	public interface HandlerContext {
		public void unregister();
	}

	private ChannelInboundHandler handler = null;
	private HandlerContext context = new HandlerContext() {
		public void unregister() {
			handler = null;
		};
	};

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;

			switch (request.getUri()) {
			case "":
			case "/":
				installHandler(ctx, new DefaultHandler(context));
				break;
			case "/game":
				installHandler(ctx, new NotificationHandler(context));
				break;
			default:
				installHandler(ctx, null);
			}
		}

		if (handler != null) {
			super.channelRead(ctx, msg);
		} else {
			ctx.writeAndFlush(create404());
			ReferenceCountUtil.release(msg);
		}
	}

	private void installHandler(ChannelHandlerContext ctx,
			ChannelInboundHandler newHandler) {
		if (handler != null) {
			ctx.pipeline().removeLast();
			handler = null;
		}
		if (newHandler != null) {
			ctx.pipeline().addLast(newHandler);
			handler = newHandler;
		}
	}

	private FullHttpResponse create404() {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		HttpHeaders.setKeepAlive(response, true);
		HttpHeaders.setContentLength(response, 0);

		return response;
	}
}