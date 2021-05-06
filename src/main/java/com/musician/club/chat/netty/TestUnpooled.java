package com.musician.club.chat.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class TestUnpooled {

    public static void main(String[] args) {
        //UnPooled是一个工具类，提供了飞驰华的ByteBuf创建、组合、复制等操作
        //这里进介绍其跟[0拷贝]相关的wrappedBuffer方法，可以用来包装ByteBuf

        ByteBuf byteBuf1 = ByteBufAllocator.DEFAULT.buffer();
        byteBuf1.writeBytes(new byte[]{1, 2, 3, 4, 5});

        ByteBuf byteBuf2 = ByteBufAllocator.DEFAULT.buffer();
        byteBuf2.writeBytes(new byte[]{6, 7, 8, 9, 0});

        //当包装byteBuf个数超过一个时，底层使用了CompositeByteBuf
        ByteBuf byteBuf3 = Unpooled.wrappedBuffer(byteBuf1, byteBuf2);
        System.out.println(ByteBufUtil.prettyHexDump(byteBuf3));

        //也可以用来包装普通字节数组，底层也不会有拷贝操作
        ByteBuf byteBuf4 = Unpooled.wrappedBuffer(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        System.out.println(byteBuf4.getClass());
        System.out.println(ByteBufUtil.prettyHexDump(byteBuf4));

    }

}
