package com.musician.club.chat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;

public class TestEventLoopServer {


    /**
     * 创建独立的EventLoopGroup，不是让NIO的EventLoopGroup去处理耗时的操作
     * @param args
     */
    public static void main(String[] args) {
        //创建独立EventLoopGroup
        EventLoopGroup eventLoopGroup = new DefaultEventLoopGroup();     //除了io事件，能处理普通任务，定时任务
        new ServerBootstrap()
                //NioEventLoop 按职责分为 BOSS和WORKER，下面注释的
                //第一个参数，Boss只负责ServerSocketChannel上的accept事件，只会创建一个线程与NioServerSocketChannel绑定
                //第二个参数，Worker负责socketChannel上的读写事件
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
//                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //这样写的话，使用的是上面的第二个worker NioEventLoopGroup中进行的
                        nioSocketChannel.pipeline()
                            .addLast("handler1",new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    System.out.println(Thread.currentThread().getName() + "=====>" +
                                            byteBuf.toString(Charset.defaultCharset()));
                                    ctx.fireChannelRead(msg);    //传递给下一个handler
                                }
                            })
                            //这样写的话，以后再对应执行handler时候就是在上面的DefaultEventLoop中执行
                            .addLast(eventLoopGroup, "handler2", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    //                                super.channelRead(ctx, msg);
                                    //没有了StringDecoder，拿到的msg就是Netty中的ByteBuf，和NIO中的ByteBuffer差不多，但是更好用了
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    System.out.println(Thread.currentThread().getName() + "======>" + ctx + "=========" +
                                            byteBuf.toString(Charset.defaultCharset()));

                                }
                        });
                    }
                }).bind(8080);
    }

    public static void main1(String[] args) {
        new ServerBootstrap()
                //NioEventLoop 按职责分为 BOSS和WORKER，下面注释的
                //第一个参数，Boss只负责ServerSocketChannel上的accept事件，只会创建一个线程与NioServerSocketChannel绑定
                //第二个参数，Worker负责socketChannel上的读写事件
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
//                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                super.channelRead(ctx, msg);
                                //没有了StringDecoder，拿到的msg就是Netty中的ByteBuf，和NIO中的ByteBuffer差不多，但是更好用了
                                ByteBuf byteBuf = (ByteBuf) msg;
                                System.out.println(ctx + "=========" + byteBuf.toString(Charset.defaultCharset()));

                            }
                        });
                    }
                }).bind(8080);
    }

}
