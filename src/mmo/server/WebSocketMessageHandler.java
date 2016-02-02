package mmo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import mmo.server.message.Message;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by flori on 02.02.2016.
 */
@AutoFactory
public class WebSocketMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ObjectReader reader;


    private static final Logger L = Logger.getAnonymousLogger();

    @Inject
    public WebSocketMessageHandler(@Provided ObjectMapper mapper) {
        reader = mapper.reader(Message.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Message m = reader.readValue(new ByteBufInputStream(msg.content()));
        ctx.fireChannelRead(m);
    }
}
