package com.musician.club.chat.bytebuffer;

import javax.swing.plaf.nimbus.AbstractRegionPainter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestByteBuffer {

    public static void main(String[] args) {
        //读取文件 FileChannel
        //1.输入流 2.RandomAccessFile

        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            //准备缓冲区 allocate划分一块内存 10个字节
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            while (true) {
                //从channel读取数据，先向ByteBuffer写入
                int len = channel.read(byteBuffer);
                if (len == -1) {
                    break;
                }
                //打印buffer中的内容
                byteBuffer.flip();//切换到byteBuffer的读模式
                while (byteBuffer.hasRemaining()) { //是否还剩余未读的数据
                    byte b = byteBuffer.get();
                    System.out.println((char) b);   //强转成字符打印
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
