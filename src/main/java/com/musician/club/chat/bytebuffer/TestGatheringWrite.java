package com.musician.club.chat.bytebuffer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class TestGatheringWrite {

    public static void main(String[] args) {
        ByteBuffer byteBuffer1 = StandardCharsets.UTF_8.encode("hello");
        ByteBuffer byteBuffer2 = StandardCharsets.UTF_8.encode("world");
        ByteBuffer byteBuffer3 = StandardCharsets.UTF_8.encode("heihei");
        try (FileChannel fileChannel = new RandomAccessFile("words.txt", "rw").getChannel()) {
            fileChannel.write(new ByteBuffer[]{byteBuffer1, byteBuffer2, byteBuffer3});

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
