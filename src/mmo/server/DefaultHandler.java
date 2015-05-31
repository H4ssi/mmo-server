package mmo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class DefaultHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		try {
			if (msg instanceof HttpRequest) {
				ByteBuf buf = Unpooled.wrappedBuffer("hello world!"
						.getBytes(CharsetUtil.UTF_8));
				HttpResponse res = new DefaultFullHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
				HttpHeaders.setHeader(res, HttpHeaders.Names.CONTENT_TYPE,
						"text/plain; encoding=utf-8");
				HttpHeaders.setKeepAlive(res, true);
				HttpHeaders.setContentLength(res, buf.readableBytes());

				ctx.writeAndFlush(res);
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}
}
