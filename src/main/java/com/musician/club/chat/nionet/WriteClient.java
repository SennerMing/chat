package com.musician.club.chat.nionet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteClient {

    public static void main(String[] args) {

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));
            int count = 0;
            while (true) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
                count += socketChannel.read(byteBuffer);
                System.out.println("读到的数据量：" + count);
                byteBuffer.clear();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
