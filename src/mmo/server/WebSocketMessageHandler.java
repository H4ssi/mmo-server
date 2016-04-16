package mmo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import mmo.server.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by flori on 02.02.2016.
 */
@AutoFactory
public class WebSocketMessageHandler extends MessageToMessageCodec<TextWebSocketFrame, Message> {
	private final ObjectReader reader;
	private final ObjectWriter writer;

	private static final Logger L = LoggerFactory.getLogger(WebSocketMessageHandler.class);

	@Inject
	public WebSocketMessageHandler(@Provided ObjectMapper mapper) {
		reader = mapper.reader(Message.class);
		writer = mapper.writerFor(Message.class);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		L.trace("writing: {}", msg);
		out.add(new TextWebSocketFrame(Unpooled.wrappedBuffer(writer.writeValueAsBytes(msg))));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg, List<Object> out) throws Exception {
		Message m = reader.readValue(new ByteBufInputStream(msg.content()));

		L.trace("read: {}", m);
		out.add(m);
	}
}
