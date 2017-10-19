package com.osight.monitor.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorClientHandler extends SimpleChannelInboundHandler<String> {
    private String clientId;

    MonitorClientHandler(String clientId) {
        super(true);
        this.clientId = clientId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String[] array = msg.split("@");
        String type = array[0];
        if ("0".equals(type)) {
            ctx.channel().writeAndFlush("2@" + clientId + "@i am ok");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    ctx.channel().writeAndFlush("0@" + clientId);
                    break;
                default:
                    break;
            }
        }


    }
}
