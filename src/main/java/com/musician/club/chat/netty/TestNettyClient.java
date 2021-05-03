package com.musician.club.chat.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class TestNettyClient {


    public static void main(String[] args) {
        //1.客户端启动类
        try {
            new Bootstrap()
                    //2.添加客户端的EventLoop
                    .group(new NioEventLoopGroup())
                    //3.选择客户端channel实现
                    .channel(NioSocketChannel.class)
                    //4.添加对应的channel处理初始化器
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        //连接建立后被调用
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new StringEncoder());
                        }
                    })
                    //5.进行服务器的连接
                    .connect(new InetSocketAddress("127.0.0.1", 8080))
                    .sync() //阻塞方法，直到连接建立
                    .channel()//代表连接对象
                    //6.发送消息！
                    .writeAndFlush("Hello World!"); //凡是收发数据都要走handler
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
