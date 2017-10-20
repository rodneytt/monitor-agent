package com.osight.monitor.netty;

import java.util.Date;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
@Sharable
public class MonitorClientHandler extends SimpleChannelInboundHandler<String> {
    private MonitorClient client;

    MonitorClientHandler(MonitorClient client) {
        super(true);
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("上线时间：" + new Date());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("下线时间：" + new Date());
        super.channelInactive(ctx);
        client.doConnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String[] array = msg.split("@");
        String type = array[0];
        if ("0".equals(type)) {
            ctx.writeAndFlush("2@" + client.getClientId() + "@i am ok");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    ctx.writeAndFlush("0@" + client.getClientId());
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
