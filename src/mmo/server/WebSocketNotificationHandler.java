package mmo.server;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import mmo.server.model.Player;

import java.util.logging.Logger;

/**
 * Created by flori on 02.02.2016.
 */
@AutoFactory
public class WebSocketNotificationHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger L = Logger.getAnonymousLogger();
    private final MessageReceiver receiver;

    public WebSocketNotificationHandler(@Provided MessageReceiverFactory receiverFactory,
                               Player player) {
        this.receiver = receiverFactory.create(player);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.receiver.init(ctx.channel());
        L.info("client connected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        receiver.receive(msg.content());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        L.info("client disconnected (channel closed)");
        receiver.exit();
        super.channelInactive(ctx);
    }
}
