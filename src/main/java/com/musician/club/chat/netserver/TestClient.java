package com.musician.club.chat.netserver;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
public class TestClient {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(8080));
            System.out.println("waiting connecting...");
//            socketChannel.write(Charset.defaultCharset().encode("hello:" + new Random().nextInt()));
            socketChannel.write(StandardCharsets.UTF_8.encode("hello\n" + "world\n"+"nihao\n"));

            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
