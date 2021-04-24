package com.musician.club.chat.bytebuffer;

import lombok.extern.slf4j.Slf4j;

import javax.swing.plaf.nimbus.AbstractRegionPainter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {

    /*
    * ByteBuffer使用
    * 1.向buffer中写入数据，例如调用channel.read(buffer)
    * 2.调用flip()切换至读模式
    * 3.从buffer读取数据，例如调用buffer.get()
    * 4.调用clear()或者compact()切换至写模式
    * 5.重复1~4步骤
    * */

    /*
    * capacity 容量
    * position 下标，指针
    * limit 读写的限制
    * */

    public static void main(String[] args) {
        //读取文件 FileChannel
        //1.输入流 2.RandomAccessFile

        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            //准备缓冲区 allocate划分一块内存 10个字节
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            while (true) {
                //从channel读取数据，先向ByteBuffer写入
                int len = channel.read(byteBuffer);
                log.info("读取到的字节数{}", len);
                if (len == -1) {
                    break;
                }
                //打印buffer中的内容
                byteBuffer.flip();//切换到byteBuffer的读模式
                while (byteBuffer.hasRemaining()) { //是否还剩余未读的数据
                    byte b = byteBuffer.get();
                    log.info("实际字节{}", (char) b);   //强转成字符打印

                }
                byteBuffer.clear();//切换为写模式
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
