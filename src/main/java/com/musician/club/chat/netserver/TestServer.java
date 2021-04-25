package com.musician.club.chat.netserver;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TestServer {



    /**
     * 这个东西呢，就是非阻塞的channel，让CPU一只空转也不好啊，所以说就得用到selector
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //使用 nio 工具类 来理解阻塞编程

        Selector selector = Selector.open();

        //创建一个服务器
        ServerSocketChannel serverSocketChannel = null;

        serverSocketChannel = ServerSocketChannel.open();
        //一句话改为非阻塞模式，下面的accept，就会不阻塞啦，没有连接的话就是返回null
        serverSocketChannel.configureBlocking(false);
        //绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(8080));

        //将channel给注册到selector上[将channel与selector建立联系]
        // SelectionKey 就是将来可以通过这个key获得哪个channel发生的事件，事件有四种类型[accept,connect,read,write]
        SelectionKey serverSocketSelectionKey = serverSocketChannel.register(selector, 0, null);

        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        List<SocketChannel> socketChannelList = new ArrayList<>();
        //接受连接与数据的读写
        while (true) {

            //连接后产生了一个SocketChannel对象,accept()是一个阻塞方法
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                System.out.println("connecting...");
                //将对应的socketChannel放入一个全局的channelList中
                socketChannel.configureBlocking(false); //这样的话channel.read()就不会阻塞啦！,如果没有读到数据那么就返回0
                socketChannelList.add(socketChannel);
                System.out.println("connected..."+ socketChannel);
            }
            //接受客户端发送的数据
            for (SocketChannel channel : socketChannelList) {
                int read = channel.read(byteBuffer);
                if (read > 0) {
                    System.out.println("before read...");
                    //read()方法默认也是阻塞的

                    byteBuffer.flip();
                    String message = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                    System.out.println(message);

                    byteBuffer.clear();
                    System.out.println("after read...");
                }


            }

        }



    }

    /**
     * 这个东西呢，就是非阻塞的channel，让CPU一只空转也不好啊，所以说就得用到selector
     * @param args
     * @throws IOException
     */
    public static void main1(String[] args) throws IOException {
        //使用 nio 工具类 来理解阻塞编程

        //1.创建一个服务器
        ServerSocketChannel serverSocketChannel = null;

        serverSocketChannel = ServerSocketChannel.open();
        //一句话改为非阻塞模式，下面的accept，就会不阻塞啦，没有连接的话就是返回null
        serverSocketChannel.configureBlocking(false);


        //2.绑定端口号

        serverSocketChannel.bind(new InetSocketAddress(8080));

        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        List<SocketChannel> socketChannelList = new ArrayList<>();
        //3.接受连接与数据的读写
        while (true) {

            //连接后产生了一个SocketChannel对象,accept()是一个阻塞方法
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                System.out.println("connecting...");
                //将对应的socketChannel放入一个全局的channelList中
                socketChannel.configureBlocking(false); //这样的话channel.read()就不会阻塞啦！,如果没有读到数据那么就返回0
                socketChannelList.add(socketChannel);
                System.out.println("connected..."+ socketChannel);
            }
            //4.接受客户端发送的数据
            for (SocketChannel channel : socketChannelList) {
                int read = channel.read(byteBuffer);
                if (read > 0) {
                    System.out.println("before read...");
                    //read()方法默认也是阻塞的

                    byteBuffer.flip();
                    String message = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                    System.out.println(message);

                    byteBuffer.clear();
                    System.out.println("after read...");
                }


            }

        }



    }

}
