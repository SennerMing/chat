package com.musician.club.chat.netty;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TestNettyPromise {

    public static void main(String[] args) {

        //创建Promise对象
        EventLoop eventLoop = new NioEventLoopGroup().next();
        DefaultPromise<Integer> defaultPromise = new DefaultPromise<>(eventLoop);

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始进行计算任务......");
                try {
                    int i = 1 / 0;
                    TimeUnit.SECONDS.sleep(1);
                    defaultPromise.setSuccess(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    defaultPromise.setFailure(e);
                }
            }
        }).start();

        System.out.println("等待执行结果......");
        try {
            System.out.println("获得执行结果：" + defaultPromise.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

}
