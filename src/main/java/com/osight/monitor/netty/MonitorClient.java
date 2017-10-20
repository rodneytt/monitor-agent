package com.osight.monitor.netty;


import java.util.concurrent.TimeUnit;

import com.osight.monitor.util.RandomUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author chenw <a href="mailto:chenw@chsi.com.cn">chen wei</a>
 * @version $Id$
 */
public class MonitorClient {
    private int port;
    private String host;
    private Bootstrap bootstrap;
    private Channel channel;


    private String clientId;

    public MonitorClient(String host, int port) {
        this.port = port;
        this.host = host;
        clientId = RandomUtils.getRandomString(16);
        start();
    }

    private void start() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(0, 10, 0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new MonitorClientHandler(MonitorClient.this));
            }

        });
        doConnect();
    }

    void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }
        ChannelFuture future = bootstrap.connect(host, port);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                    channel = f.channel();
                    System.out.println("connect server success---------");
                } else {
                    System.out.println("connect server fail, try connect after 3s");
                    f.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 3, TimeUnit.SECONDS);
                }
            }
        });
    }

    public void send(String msg) {
        if (channel != null && channel.isActive()) {
            String m = String.format("1@%s@%s", clientId, msg);
            channel.writeAndFlush(m);
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public static void main(String[] args) {
        final MonitorClient monitorClient;
        try {
            monitorClient = new MonitorClient("localhost", 8709);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    while (i++ <= 1000) {
                        try {
                            Thread.sleep(1000);
                            monitorClient.send("this is " + Thread.currentThread().getName() + ":" + i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread t1 = new Thread(runnable);
            t1.start();
            t1.join();
            monitorClient.close();
            System.out.println("测试完成");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getClientId() {
        return clientId;
    }
}
