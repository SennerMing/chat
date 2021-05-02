package com.musician.club.chat.niobasic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannel {
    public static void main(String[] args) {
        try {
            FileChannel inputChannel = new FileInputStream("data.txt").getChannel();
            FileChannel outputChannel = new FileOutputStream("cp-data.txt").getChannel();

            //效率高，使用操作系统的zero-copy，传输上线是2GB
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);

            //改进,2GB以上
            long size = inputChannel.size();
            for (long left = size; left > 0; ) {
                System.out.println("position:"+(size-left)+"，left:"+left);
                left -= inputChannel.transferTo(size - left, left, outputChannel);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
