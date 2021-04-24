package com.musician.club.chat.bytebuffer;

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

            inputChannel.transferTo(0, inputChannel.size(), outputChannel);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
