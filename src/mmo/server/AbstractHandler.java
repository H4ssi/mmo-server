package mmo.server;

import io.netty.channel.ChannelInboundHandlerAdapter;
import mmo.server.Handler.HandlerContext;

public class AbstractHandler extends ChannelInboundHandlerAdapter {

	private final HandlerContext handlerContext;

	public AbstractHandler(HandlerContext handlerContext) {
		super();
		this.handlerContext = handlerContext;
	}

	public HandlerContext getHandlerContext() {
		return handlerContext;
	}
}