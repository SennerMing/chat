package com.musician.club.chat.niobasic;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestByteBufferSplit {

    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.put("Hello,World\nI'm zhangsan\nHo".getBytes());
        split(byteBuffer);
        byteBuffer.put("w are you\n".getBytes());
        split(byteBuffer);
    }

    private static void split(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        for (int i = 0; i < byteBuffer.limit(); i++) {
            if (byteBuffer.get(i) == '\n') {
                int message_length = i + 1 - byteBuffer.position();
                //将消息存入完整ByteBuffer,一次读入message_length这么长的数据
                ByteBuffer message = ByteBuffer.allocate(message_length);
                for (int j = 0; j < message_length; j++) {
//                    System.out.println((char)byteBuffer.get());
//                    message.put(byteBuffer.get());
                    message.put(byteBuffer.get());

                }
                //可以做标记，重复读 message.mark();
                message.flip(); //切换成读模式
                String message_str = StandardCharsets.UTF_8.decode(message).toString();
                System.out.println(message_str);
                message.compact(); //切换成写模式
                //message.reset();回到mark标记的地方
            }
        }
        byteBuffer.compact();

    }

}
