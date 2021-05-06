package com.musician.club.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class TestNettySlice {

    public static void main(String[] args) {

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
        byteBuf.writeBytes("hello,i am Tony".getBytes());
        log(byteBuf);

        //由于内存地址和byteBuf相同，不能再进行增加了，IndexOutOfBoundsException
        ByteBuf byteBuf1 = byteBuf.slice(0, 5);
        ByteBuf byteBuf2 = byteBuf.slice(5, 5);
        log(byteBuf1);
        log(byteBuf2);

        System.out.println("=============================");
        byteBuf1.setByte(0, 'H');
        log(byteBuf1);
        log(byteBuf);

        //对原有byteBuf进行释放操作，再进行byteBuf1和byteBuf2的使用时，就会报错了，
        //可以使用byteBuf的retain()方法就能让ByteBuf的引用保留
//        但是不要忘记用完byteBuf1或byteBuf2进行释放
//        byteBuf.release();
//        byteBuf.duplicate();  //复制0拷贝
//        byteBuf.copy() //深拷贝后续操作与byteBuf无关

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
