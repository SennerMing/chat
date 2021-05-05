package com.musician.club.chat.netty;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class TestNettyFuture {

    public static void main(String[] args) {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

        EventLoop eventLoop = nioEventLoopGroup.next();

        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                System.out.println("执行计算中......");
                TimeUnit.SECONDS.sleep(1);
                return 70;
            }
        });

        //同步阻塞的
//        System.out.println("等待结果中.....");
//        try {
//            System.out.println("计算结果：" + future.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        //异步方式
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                //后面就不需要get()阻塞了，直接getNow就行
                System.out.println(future.getNow());
            }
        });



    }


}
