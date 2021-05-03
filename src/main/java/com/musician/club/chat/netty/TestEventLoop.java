package com.musician.club.chat.netty;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;

import java.util.concurrent.TimeUnit;

public class TestEventLoop {

    public static void main(String[] args) {

        //1.创建事件循环组，既能处理io事件，普通任务，也能处理定时任务
        //那么内部会创建几个事件循环对象呢？一个线程对应一个事件循环对象，默认是多少个呢？
//        System.out.println(NettyRuntime.availableProcessors()*2);   //可以通过此函数获得
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2); //指定2个线程
//        EventLoopGroup eventLoopGroup1 = new DefaultEventLoop(); //除了不能处理io事件和上面的一样
//        获取下一个事件循环对象，简单地负载均衡
        System.out.println(eventLoopGroup.next());  //由于这边指定了2个线程，那么第一个next()获得的是第一个事件循环对象
        System.out.println(eventLoopGroup.next());  //那么第二个next()获得的是第二个事件循环对象
        System.out.println(eventLoopGroup.next());  //这个由获取了第一个事件循环对象

        //让他执行个普通任务，意义：1.异步处理，比较耗时的工作；2.事件分发，一个线程转移到另一个线程
        eventLoopGroup.next().submit(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("task ok");
        });   //继承了线程池，有很多线程池的方法


        //让他执行一个定时任务  keep alive 心跳吧
        eventLoopGroup.next().scheduleAtFixedRate(() -> {
            System.out.println("schedule ok");
        }, 0, 1, TimeUnit.SECONDS);//0：initial delay    1：period


        System.out.println("main end");

    }

}
