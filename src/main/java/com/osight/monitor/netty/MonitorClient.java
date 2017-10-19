package com.osight.monitor.netty;


import com.osight.monitor.util.RandomUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
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

    private SocketChannel socketChannel;


    private String clientId;

    public MonitorClient(String host, int port) {
        this.port = port;
        this.host = host;
        clientId = RandomUtils.getRandomString(16);
        start();
    }

    private void start() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(20, 10, 0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new MonitorClientHandler(clientId));
            }

        });
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                System.out.println("connect server  成功---------");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        String m = String.format("1@%s@%s", clientId, msg);
        socketChannel.writeAndFlush(m);
    }

    public void close() {
        if (socketChannel != null) {
            socketChannel.close();
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
                        monitorClient.send("this is " + Thread.currentThread().getName() + ":" + i);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread t1 = new Thread(runnable);
            Thread t2 = new Thread(runnable);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            monitorClient.close();
            System.out.println("测试完成");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
