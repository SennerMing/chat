package com.musician.club.chat.bytebuffer;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestByteBufferString {

    public static void main(String[] args) {
        /**
         * ByteBuffer转String
         */
        //普通ByteBuffer转字符串(继续还是写模式)
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        try {
            byteBuffer.put("hello".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //字符集  会自动切换到读模式(改变position)
        ByteBuffer byteBuffer1 = StandardCharsets.UTF_8.encode("hello");

        //wrap  会自动切换到读模式(改变position)
        ByteBuffer byteBuffer2 = ByteBuffer.wrap("hello".getBytes());

        /*
        * String 转ByteBuffer
        * */

        byteBuffer.flip();

        String str0 = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        log.info("1.ByteBuffer转换成的String:{}", str0);

        String str1 = StandardCharsets.UTF_8.decode(byteBuffer1).toString();
        log.info("2.ByteBuffer转换成的String:{}", str1);

        String str2 = StandardCharsets.UTF_8.decode(byteBuffer2).toString();
        log.info("3.ByteBuffer转换成的String:{}", str2);
    }

}
