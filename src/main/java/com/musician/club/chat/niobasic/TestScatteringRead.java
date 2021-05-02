package com.musician.club.chat.niobasic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class TestScatteringRead {

    public static void main(String[] args) {
        /*
        * FileChannel 使用 FileInputStream 只能读
        * FileChannel 使用 FileOutputStream 只能写
        * FileChannel 会用 RandomAccessFile 读写都可以
        * 1、读取：
        * channel.read(byteBuffer) 读入ByteBuffer长度个数据，返回-1时，表示已经将Channel中内容读完
        * 2、写入
        * byteBuffer.flip()
        * while(byteBuffer.hasRemaining()){
        *   channel.write(byteBuffer)
        * }
        * 写入的数据并不是实时刷新到磁盘的
        * 3、关闭
        * channel.close(); 调用了FileInputStream、FileOutputStream或者RandomAccessFile的关闭流方法，
        * 会间接的关闭channel
        *
        * */

        try (FileChannel fileChannel = new RandomAccessFile("words.txt", "r").getChannel()) {
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(3);
            ByteBuffer byteBuffer2 = ByteBuffer.allocate(3);
            ByteBuffer byteBuffer3 = ByteBuffer.allocate(5);
            fileChannel.read(new ByteBuffer[]{byteBuffer1, byteBuffer2, byteBuffer3});
            byteBuffer1.flip();
            byteBuffer2.flip();
            byteBuffer3.flip();

            String str1 = StandardCharsets.UTF_8.decode(byteBuffer1).toString();
            String str2 = StandardCharsets.UTF_8.decode(byteBuffer2).toString();
            String str3 = StandardCharsets.UTF_8.decode(byteBuffer3).toString();

            System.out.println(str1 + " " + str2 + " " + str3);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
