package com.musician.club.chat.netty;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TestJdkFuture {

    public static void main(String[] args) {

        //1.创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        //2.提交任务
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("线程池执行中......");
                TimeUnit.SECONDS.sleep(1);
                return 50;
            }
        });
        //3.主线程通过future来获取结果
        try {
            System.out.println("等待线程执行结果......");
            System.out.println("线程执行结果："+future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

}
