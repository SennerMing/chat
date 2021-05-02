package com.musician.club.chat.nionet;

import org.apache.catalina.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class WriteServer {

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(8080));

            Selector serverSelector = Selector.open();
            serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

            while (true) {
                serverSelector.select();//这样只会在有事件发生时才会继续往下执行
                Iterator<SelectionKey> selectionKeyIterator = serverSelector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel)selectionKey.channel();
                        SocketChannel clientSocketChannel = serverSocketChannel1.accept();
                        clientSocketChannel.configureBlocking(false);
                        SelectionKey clientSelectionKey = clientSocketChannel.register(serverSelector, SelectionKey.OP_READ); //也可以写成下面两行
//                        SelectionKey clientSelectionKey = socketChannel.register(serverSelector, 0, null);    //0就表示不关注任何事件
//                        clientSelectionKey.interestOps(SelectionKey.OP_READ);

                        //向客户端发送大量数据
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < 300000; i++) {
                            sb.append("a" + i);
                        }
                        ByteBuffer byteBuffer = Charset.defaultCharset().encode(sb.toString());
                        /*
                            这样写的话，会影响服务器的效率，因为客户端那边也会有缓冲满的时候，就写不进去了
                            //返回值表示实际写入的字节数
                            while (byteBuffer.hasRemaining()) {
                                int actualWrite = socketChannel.write(byteBuffer);
                                System.out.println("实际写入：" + actualWrite);
                            }
                        */
                        //****************************** 改进版本！！！！！！！********************
                        //对应关注可写事件，才能解决上面的问题
                        int write_count = clientSocketChannel.write(byteBuffer);
                        System.out.println("实际写入了：" + write_count);
                        if (byteBuffer.hasRemaining()) {
                            clientSelectionKey.interestOps(clientSelectionKey.interestOps() + SelectionKey.OP_WRITE);
//                            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);//位运算符写法
                            //把未写完的数据加入到SelectionKey对应的Channel中
                            clientSelectionKey.attach(byteBuffer);
                        }

                    } else if (selectionKey.isWritable()) {

                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        int write_count = socketChannel.write(byteBuffer);
                        System.out.println("实际写入了：" + write_count);

                        if (!byteBuffer.hasRemaining()) {
                            //写完了的话，释放掉attachment，并且将关注的写事件去掉
                            selectionKey.attach(null);
                            selectionKey.interestOps(selectionKey.interestOps() - SelectionKey.OP_WRITE);
                        }

                    }


                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
