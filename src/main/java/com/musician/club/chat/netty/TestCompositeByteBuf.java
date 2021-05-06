package com.musician.club.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class TestCompositeByteBuf {

    public static void main(String[] args) {
        ByteBuf byteBuf1 = ByteBufAllocator.DEFAULT.buffer();
        byteBuf1.writeBytes("12345".getBytes());

        ByteBuf byteBuf2 = ByteBufAllocator.DEFAULT.buffer();
        byteBuf2.writeBytes("67890".getBytes());

        //0拷贝，注意retain()与release()
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeByteBuf.addComponents(true, byteBuf1, byteBuf2);
        log(compositeByteBuf);

    }

    private static void log(ByteBuf byteBuf) {
        int length = byteBuf.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder sb = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(byteBuf.readerIndex())
                .append(" write index:").append(byteBuf.writerIndex())
                .append(" capacity:").append(byteBuf.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(sb, byteBuf);
        System.out.println(sb.toString());

    }

}
