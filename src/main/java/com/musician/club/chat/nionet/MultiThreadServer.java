package com.musician.club.chat.nionet;

import org.hibernate.jdbc.Work;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadServer {

    /**
     * 现在都是多核CPU，设计师要充分考虑别让多核的CPU能力浪费掉
     *
     * 前面的代码都只有一个选择器，没有充分利用多核CPU，如何改进？
     *
     * 分两组选择器
     *
     * - 单线程配一个选择器，专门用来处理accpet事件
     * - 创建CPU核心数的线程，每一个线程配一个选择器，轮流处理read事件
     * @param args
     */

    public static void main(String[] args) {
        Thread.currentThread().setName("BOSS");
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8080));
            serverSocketChannel.configureBlocking(false);

            Selector serverSelector = Selector.open();
            serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

            //创建Worker
//            Worker worker = new Worker("Worker");
//            Worker[] workers = new Worker[2];
            //动态获取核心数,JDK10之后才支持Docker容器的真实核数，计算密集型，尽量设置线程为核心数；IO密集型，根据阿尔达姆定律，大于核心数
            Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
            for (int i = 0; i < workers.length; i++) {
                workers[i] = new Worker("worker-" + i);

            }
            AtomicInteger atomicInteger = new AtomicInteger();

            while (true) {
                serverSelector.select();
                Iterator<SelectionKey> selectionKeyIterator = serverSelector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel clientSocketChannel = serverSocketChannel1.accept();
                        clientSocketChannel.configureBlocking(false);
                        //关联上worker
                        System.out.println("connecting..." + clientSocketChannel.getRemoteAddress());
                        //轮询 round ribbon

                        System.out.println("before register..." + clientSocketChannel.getRemoteAddress());
                        workers[atomicInteger.incrementAndGet() % workers.length].register(clientSocketChannel);
                        System.out.println("after register..." + clientSocketChannel.getRemoteAddress());

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class Worker implements Runnable {
        private String name;
        private Thread thread;
        private Selector selector;
        private ConcurrentLinkedDeque<Runnable> runnableConcurrentLinkedDeque = new ConcurrentLinkedDeque<>();
        private volatile boolean isFirst = false;

        public Worker(String name) {
            this.name = name;
        }

        public void register(SocketChannel socketChannel) {
            if (!isFirst) {
                thread = new Thread(this, name);
                thread.start();
                try {
                    selector = Selector.open();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isFirst = true;
            }

            runnableConcurrentLinkedDeque.add(() -> {
                try {
                    socketChannel.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });

            //唤醒下面的select方法，继续往下执行，实际就是去队里中取任务，将队列中的任务执行，上面的socketChannel.register()
            //那么就会给socketChannel监听Read事件
            selector.wakeup();  //selector.select()之前发车票，selector.select()之后发车票

        }

        @Override
        public void run() {
            while (true) {
                try {
                    //控制阻塞的地方有两个：1.客户端的事件发生，2.上面register()方法的主动wakeup()
                    selector.select();
                    Runnable task = runnableConcurrentLinkedDeque.poll();
                    if (task != null) {
                        task.run();
                    }
                    Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();
                        if (selectionKey.isReadable()) {
                            System.out.println("reading...");
                            ByteBuffer byteBuffer = ByteBuffer.allocate(32);
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            socketChannel.read(byteBuffer);
                            byteBuffer.flip();
                            System.out.println(StandardCharsets.UTF_8.decode(byteBuffer).toString());
                            byteBuffer.clear();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
