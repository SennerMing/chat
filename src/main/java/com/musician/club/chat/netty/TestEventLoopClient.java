package com.musician.club.chat.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import java.net.InetSocketAddress;

public class TestEventLoopClient {



    public static void main(String[] args) throws InterruptedException{


        //像什么Future、Promise的 类型都是一部方法配套使用的，用来获得处理结果
        ChannelFuture channelFuture = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    nioSocketChannel.pipeline().addLast(new StringEncoder());
                }
            })
            //连接到服务器，异步非阻塞的，main发起了调用，但真正的连接工作，交给了另一个线程去做
            //其实就是上面的NioEventLoopGroup
            .connect(new InetSocketAddress("127.0.0.1", 8080));

        /**
         * 第一种，Future方法
         */
//            channelFuture.sync();//同步阻塞获取建立连接的结果
//            Channel channel = channelFuture.channel();
//            channel.writeAndFlush("Test eventLoop !");

        /**
         * 第二种，addListener(回调对象)方法一部处理结果,这个也是交由NioEventLoopGroup()来使用的
         */
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //NioEventLoopGroup中建立连接完成之后，就会调用该方法
                Channel channel = channelFuture.channel();
                channel.writeAndFlush("hello hello!");
            }
        });


    }

    public static void main1(String[] args) {
        /**
         * 一旦一个channel与一个eventLoop绑定，那么这个channel以后的所有事件都交由该eventLoop进行处理
         */
        try {
            Channel channel = new Bootstrap()
                    .group(new NioEventLoopGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new StringEncoder());
                        }
                    })
                    .connect(new InetSocketAddress("127.0.0.1", 8080))
                    .sync()
                    .channel();
            System.out.println(channel);
            channel.writeAndFlush("Test eventLoop !");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
