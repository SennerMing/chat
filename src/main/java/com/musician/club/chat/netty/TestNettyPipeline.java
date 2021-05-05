package com.musician.club.chat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.catalina.Pipeline;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class TestNettyPipeline {
    public static void main(String[] args) {

        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    //head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                    //其中一个没有调用channelRead()或者fireChannelRead()都会断掉,in或out的链都会断掉
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline channelPipeline = nioSocketChannel.pipeline();
                        channelPipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println("in1");
                                ByteBuf byteBuf = (ByteBuf) msg;
                                String name = byteBuf.toString(Charset.defaultCharset());
                                super.channelRead(ctx, name);
                            }
                        });
                        channelPipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println("in2");
                                Student student = new Student(msg.toString());
                                super.channelRead(ctx, student);
                            }
                        });
                        channelPipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println("in3");
                                System.out.println(msg);
//                                super.channelRead(ctx, msg);//最后一个不需要再调用了，没有下一个handler了
//                                ctx.fireChannelRead(msg); //也可以这样去传递消息
//                                channel的writeAndFlush会从tail开始往前找
//                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("server message....".getBytes()));
                                //调用下面这个写法，他会去从当前handler之前去找下一个out 的 handler进行消息的处理
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("server message ge....".getBytes()));
                            }
                        });

                        channelPipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println("out1");
                                super.write(ctx, msg, promise);
                            }
                        });
                        channelPipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println("out2");
                                super.write(ctx, msg, promise);
                            }
                        });
                        channelPipeline.addLast("h6", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                System.out.println("out3");
                                super.write(ctx, msg, promise);
                            }
                        });

                    }
                }).bind(new InetSocketAddress("127.0.0.1", 8080));


    }


    @Data
    @AllArgsConstructor
    static class Student {
        private String name;
    }

}

