package com.musician.club.chat.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Random;

public class TestPacketProblem {


    public static void main(String[] args) {

//        System.out.println(fill10Bytes('1', 5));



//        for (int i = 0; i < 10; i++) {
//            send();
//        }

        send();

    }

    private static byte[] fill10Bytes(char c, int len) {
        byte[] bytes = new byte[10];
        for (int i = 0; i < 10; i++) {
            if (i < len) {
                bytes[i] = (byte) c;
            }else{
                bytes[i] = '_';
            }
        }
        return bytes;
    }


    private static void send() {

        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(16);
//                                byteBuf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17});
//                                ctx.writeAndFlush(byteBuf);
//                                ctx.channel().close();

                                ByteBuf byteBuf = ctx.alloc().buffer();
                                char c = '0';
                                for (int i = 0; i < 10; i++) {
                                    byte[] bytes = fill10Bytes(c, new Random().nextInt(10));
                                    c++;
                                    byteBuf.writeBytes(bytes);
                                }
                                ctx.writeAndFlush(byteBuf);
                                ctx.channel().close();
                            }
                        });
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            nioEventLoopGroup.shutdownGracefully();
        }


    }


}
