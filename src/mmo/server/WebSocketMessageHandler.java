package mmo.server;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import mmo.server.message.Message;

/**
 * Created by flori on 02.02.2016.
 */
@AutoFactory
public class WebSocketMessageHandler extends ChannelDuplexHandler {
	private final ObjectReader reader;
	private final ObjectWriter writer;

	private static final Logger L = LoggerFactory.getLogger(WebSocketMessageHandler.class);

	@Inject
	public WebSocketMessageHandler(@Provided ObjectMapper mapper) {
		reader = mapper.reader(Message.class);
		writer = mapper.writerFor(Message.class);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof TextWebSocketFrame) {
			Message m = reader.readValue(new ByteBufInputStream(((TextWebSocketFrame) msg).content()));
			L.trace("read: {}", m);
			ReferenceCountUtil.release(msg);
			ctx.fireChannelRead(m);
		} else {
			// TODO what to do in case of unexpected msg?
			L.warn("unexpected msg received: {}", msg);
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// TODO why do I get messages that I do not want to receive???
		if (msg instanceof Message) {
			L.trace("writing: {}", msg);
			ctx.write(new TextWebSocketFrame(Unpooled.wrappedBuffer(writer.writeValueAsBytes(msg))), promise);
		} else {
			ctx.write(msg, promise);
		}
	}
}
