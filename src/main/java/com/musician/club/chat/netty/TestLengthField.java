package com.musician.club.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

public class TestLengthField {

    public static void main(String[] args) {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 0),
                new LoggingHandler(LogLevel.INFO)
        );

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        fillByteBuf(byteBuf, "Hello,World!");
        fillByteBuf(byteBuf,"Hi,How are you?");
        embeddedChannel.writeInbound(byteBuf);

    }

    private static void fillByteBuf(ByteBuf byteBuf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;
        byteBuf.writeInt(length);
        byteBuf.writeBytes(bytes);
    }


}
