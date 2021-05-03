package com.musician.club.chat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class TestNettyServer {
    public static void main(String[] args) {
        //1.启动器，负责组装Netty组件
        new ServerBootstrap()
                //2.BOSS eventLoop ，之前Worker eventLoop(selector,thread)，这里加入了一个事件循环组
                .group(new NioEventLoopGroup())
                //3.OIO ordinary io，BIO blocking io，选择服务器的ServerSocket实现
                .channel(NioServerSocketChannel.class)
                //4.Boss负责处理连接， worker(child)负责处理读写，决定了之后的worker能做哪些操作
                .childHandler(new ChannelInitializer<NioSocketChannel>() {//代表和客户端进行读写的通道初始化器
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //5.添加具体的handler，将ByteBuf转换为字符串
                        nioSocketChannel.pipeline().addLast(new StringDecoder());
                        //6.自定义的handler
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                //进行读的处理打印第5步处理好的字符串信息
                                super.channelRead(ctx, msg);
                                System.out.println(msg);
                            }
                        });
                    }
                    //7.绑定监听端口
                }).bind(8080);
    }

}
