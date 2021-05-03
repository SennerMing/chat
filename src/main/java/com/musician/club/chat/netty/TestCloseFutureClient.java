package com.musician.club.chat.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class TestCloseFutureClient {


    /**这玩意到底怎么改造才能正确的在结束后，做善后处理呢？
     *
     * @param args
     */
    public static void main(String[] args) {
        //为了完全关闭客户端
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

        ChannelFuture channelFuture = new Bootstrap()
                //虽然用户输入了"q"进行退出，那么为什么线程还是没有关闭呢？就是因为这个NioEventLoopGroup没有进行关闭
                .group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        //调试handler很有用的
                        channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        channel.pipeline().addLast(new StringEncoder());
                    }

                }).connect(new InetSocketAddress("127.0.0.1", 8080));
        try {
            Channel channel = channelFuture.sync().channel();
            new Thread(()->{
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String line = scanner.nextLine();
                    if ("q".equals(line)) {
                        channel.close();//close也是一个异步操作
                        break;
                    }else{
                        channel.writeAndFlush(line);
                    }
                }
            },"input").start();

            //获取closedFuture对象 1.通不处理关闭 2.异步处理关闭
            ChannelFuture closeFuture = channel.closeFuture();
//            System.out.println("waiting close");
//            closeFuture.sync();
//            System.out.println("可以在此进行安全的善后处理了！");

            //服务器端也可以进行如此的处理
            closeFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                System.out.println("可以在此进行安全的善后处理了！");
                nioEventLoopGroup.shutdownGracefully();//不会立即停止，会先拒绝接收新的任务，把手头的活先干完，再进行停止！
            });


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main1(String[] args) {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        //调试handler很有用的
                        channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        channel.pipeline().addLast(new StringEncoder());
                    }

                }).connect(new InetSocketAddress("127.0.0.1", 8080));
        try {
            Channel channel = channelFuture.sync().channel();
            new Thread(()->{
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String line = scanner.nextLine();
                    if ("q".equals(line)) {
                        channel.close();//close也是一个异步操作
                        break;
                    }else{
                        channel.writeAndFlush(line);
                    }
                }
            },"input").start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
