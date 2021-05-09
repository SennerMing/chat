package com.musician.club.chat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class TestPacketProblemServer {

    public static void main(String[] args) {

        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //调整系统的接受缓冲区大小，滑动窗口，会自行调整，不必设置
//        serverBootstrap.option(ChannelOption.SO_RCVBUF, 10);
        //调整Netty的接收缓冲区（byteBuf）, 最小是16，其他都是16的整数倍
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,
                new AdaptiveRecvByteBufAllocator(16, 16, 16));
        serverBootstrap.group(nioEventLoopGroup, workerEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new FixedLengthFrameDecoder(10));
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress("127.0.0.1", 8080)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            nioEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        }

    }

}
