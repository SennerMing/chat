package com.musician.club.chat.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

public class TestAioFileChannel {

    public static void main(String[] args) {
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"),
                StandardOpenOption.READ)){
            //参数1 ByteBuffer
            //参数2 读取的起始位置
            //参数3 附件
            //参数4 回调对象
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);

            channel.read(byteBuffer, 0, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                /**
                 * read成功就调用此函数
                 * @param result    读到的长度
                 * @param attachment    一次读不完可以再读取一次
                 */
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    attachment.flip();
                    String message = StandardCharsets.UTF_8.decode(attachment).toString();
                    System.out.println(Thread.currentThread().getName() + "----->" +message);
                    attachment.compact();
                }

                /**
                 * 当read方法失败就调用此方法
                 * @param exc
                 * @param attachment
                 */
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("read end");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
