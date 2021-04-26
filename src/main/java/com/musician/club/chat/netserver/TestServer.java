package com.musician.club.chat.netserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestServer {


    /**
     * 动态扩容的学习，使用到了ServerSocket中attachment
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //使用 nio 工具类 来理解阻塞编程
        //1.创建一个Selector
        Selector selector = Selector.open();

        //2.创建一个服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //一句话改为非阻塞模式，下面的accept，就会不阻塞啦，没有连接的话就是返回null
        serverSocketChannel.configureBlocking(false);
        //绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(8080));


        SelectionKey serverSocketSelectionKey = serverSocketChannel.register(selector, 0, null);

        // SelectionKey 就是将来可以通过这个key获得哪个channel发生的事件
        //监听Accept事件
        serverSocketSelectionKey.interestOps(SelectionKey.OP_ACCEPT);

        //接受连接与数据的读写
        while (true) {
            //3.select方法，同样是阻塞的，如果有事件发生，那么线程才会恢复运行
            selector.select();

            //4.处理感兴趣的事件,通过selector.selectedKeys()内部包含了所有发生的事件
            Iterator<SelectionKey> selectionKeyIterable = selector.selectedKeys().iterator();
            while (selectionKeyIterable.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterable.next();
                //拿到SelectionKey就给它干掉，否则下次遍历，还是会遍历到，由于下面的逻辑会将事件消费掉，因此会引起事件获取为null的情况
                selectionKeyIterable.remove();

                //5.区分下事件类型
                if (selectionKey.isAcceptable()) {//如果是Accept事件
                    System.out.println("key:" + selectionKey);
                    ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = serverSocketChannel1.accept();
                    socketChannel.configureBlocking(false);

                    //***************** 动态扩容第一步，为接收到的Channel添加一个附件，attachment(Object) *******************
                    //将一个ByteBuffer作为一个附件关联到SelectionKey中，让ByteBuffer的生命周期同SelectionKey
                    ByteBuffer attach_byteBuffer = ByteBuffer.allocate(16);
                    SelectionKey clientSocketSelectionKey = socketChannel.register(selector, 0, attach_byteBuffer);

                    clientSocketSelectionKey.interestOps(SelectionKey.OP_READ);
                    System.out.println("connected socket channel:" + socketChannel);

                } else if (selectionKey.isReadable()) { //如果是read事件
                    try {

                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        //******************* 动态扩容第二步，将存储到SelectionKey中的attachment取回 ********************
                        //从SocketChannel中获得相应的ByteBuffer
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        int read = socketChannel.read(byteBuffer);
                        if (read == -1) { //如果未读取到数据，甭管是client正常关闭还是就没有数据发送来，就直接cancel掉！
                            selectionKey.cancel();
                        } else {
                            splitRead(byteBuffer);
                            //***************** 动态扩容第三步 进行ByteBuffer的扩展 ******************
                            //这里讲一下原因，为什么position和limit相等就证明需要扩容了
                            //因为splitRead中，循环每一个字节，如果找到了对应的分隔符，才进行数据的读取
                            //如果没有找到，则什么也不做，也就是说没有进行ByteBuffer内容的消费，
                            //即使splitRead最后进行了compact()压缩操作，因为没有进行数据的读取，所以压了个寂寞
                            //看看compact里面都干了些啥吧
                            //  position(remaining());剩下了多少未读
                            //  limit(capacity());容量是多大
                            //  可不就相等嘛
                            if (byteBuffer.position() == byteBuffer.limit()) {
                                //那么，让我们进行扩容操作吧！！
                                ByteBuffer newByteBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                byteBuffer.flip();//需要将byteBuffer重回读模式因为下面的put()方法会调用byteBuffer.get()
                                newByteBuffer.put(byteBuffer);
                                selectionKey.attach(newByteBuffer);//将扩容后的byteBuffer重新加入到了selectionKey中
                            }

                        }
                    } catch (IOException e) {
                        //客户端在强制关闭连接的时候，上面的socketChannel.read()读不到数据，会抛错误
                        //并且会触发channel中的read的事件，需要将事件进行取消！就不会不停的循环
                        selectionKey.cancel();
                        e.printStackTrace();
                    }

                } else {
                    //可以不做任何处理,事件的话，不是处理就是cancel，不能置之不理，太坏！
                    selectionKey.cancel();
                }
            }
        }
    }

    /**
     * main2中的例子其实是因为没有界定好信息边界的问题，那么这个例子就来解决它，我们将ByteBuffer的capacity变小
     * 俗称：半包，粘包问题
     *      -- 1.约定消息长度，浪费带宽
     *      -- 2.约定分隔符[TestByteBufferSplit中以\n进行分隔]，这个解决方案有问题，
     *              如果一条消息的长度比中间缓存的ByteBuffer的长度还长，就尴尬了，而且还要去找分隔符，效率很低的
     *      -- 3.一条消息，带了消息长度，这个就很棒，HTTP就是这样的
     *              Http 1.0 TLV的格式 Type Length Value，类型长度已知的情况下，就可以方便的获取消息大小，分配合适的buffer，
     *                  缺点是buffer需要提前分配，如果内容过大，则影响server的吞吐量 [Type:Content-Type]
     *              Http 2.0 LTV的格式 这个就改进了
     *  下面使用第二种方式进行半包、粘包的处理，加深下印象
     * @param args
     * @throws IOException
     */
    public static void main3(String[] args) throws IOException {
        //使用 nio 工具类 来理解阻塞编程
        //1.创建一个Selector
        Selector selector = Selector.open();

        //2.创建一个服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //一句话改为非阻塞模式，下面的accept，就会不阻塞啦，没有连接的话就是返回null
        serverSocketChannel.configureBlocking(false);
        //绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(8080));

        //将channel给注册到selector上[将channel与selector建立联系]      Ops:0就是不关注任何事件
        //这一步呢，这个selector中会创建一个集合，专门用于保存SelectionKey，
        //这一步过后，这个集合里面就拥有了一个ServerSocketChannel的SelectionKey
        SelectionKey serverSocketSelectionKey = serverSocketChannel.register(selector, 0, null);
        // SelectionKey 就是将来可以通过这个key获得哪个channel发生的事件
        //  事件有四种类型[accept,connect,read,write]
        //      -- accept 会在有连接请求时触发
        //      -- connect 是客户端，连接后触发的
        //      -- read 可读事件
        //      -- write 可写时间
        //正是因为有这么多的事件，从设计的角度上来看selectionKey还要支持对感兴趣的事件进行订阅与监听
        serverSocketSelectionKey.interestOps(SelectionKey.OP_ACCEPT);

        //接受连接与数据的读写
        while (true) {
            //3.select方法，同样是阻塞的，如果有事件发生，那么线程才会恢复运行
            selector.select();

            //4.处理感兴趣的事件,通过selector.selectedKeys()内部包含了所有发生的事件；如果selector中存在未处理的事件，则还是会疯狂循环！
            //这一步selector.selectedKeys()又会在Selector中创建一个集合，保存发生事件channel的SelectionKey
            //这个selectedKeys的集合，只会增加，并不会自动删除
            Iterator<SelectionKey> selectionKeyIterable = selector.selectedKeys().iterator();
            while (selectionKeyIterable.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterable.next();
                //拿到SelectionKey就给它干掉，否则下次遍历，还是会遍历到，由于下面的逻辑会将事件消费掉，因此会引起事件获取为null的情况
                selectionKeyIterable.remove();

                //5.区分下事件类型
                if (selectionKey.isAcceptable()) {//如果是Accept事件
                    System.out.println("key:" + selectionKey);
                    //进行selector中注册的channel中产生的selector监听的感兴趣的处理
                    ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) selectionKey.channel();
                    //下面这一步serverSocketChannel.accept()会将，selectedKeys中的accept事件标记为已处理（就将事件删掉了，
                    //但是SelectorKeys中对应的key没有被删掉，下次iteration的时候，会引起异常，因此一定要在获取SelectionKey之后删掉！
                    //就是上面的selectionKeyIterable.remove()）
                    SocketChannel socketChannel = serverSocketChannel1.accept();
                    //进行一些读取事件的处理
                    socketChannel.configureBlocking(false);
                    //虽然取消了blocking，但是为了防止他一直做无用功，那么也将它注册到selector中
                    //这一步将SocketChannel也注册到Selector上了，也就是放到了保存SelectionKey的集合中了
                    SelectionKey clientSocketSelectionKey = socketChannel.register(selector, 0, null);
                    //让他关注可读事件
                    clientSocketSelectionKey.interestOps(SelectionKey.OP_READ);
                    System.out.println("connected socket channel:" + socketChannel);

                } else if (selectionKey.isReadable()) { //如果是read事件
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(16); //由main2中的50改成了5
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//                      socketChannel.configureBlocking(false);
                        int read = socketChannel.read(byteBuffer);
                        if (read == -1) { //如果未读取到数据，甭管是client正常关闭还是就没有数据发送来，就直接cancel掉！
                            selectionKey.cancel();
                        } else {
//                            byteBuffer.flip();
//                            String message = StandardCharsets.UTF_8.decode(byteBuffer).toString();
//                            System.out.println(message);
//                            byteBuffer.clear();
                            splitRead(byteBuffer);
                        }
                    } catch (IOException e) {
                        //客户端在强制关闭连接的时候，上面的socketChannel.read()读不到数据，会抛错误
                        //并且会触发channel中的read的事件，需要将事件进行取消！就不会不停的循环
                        selectionKey.cancel();
                        e.printStackTrace();
                    }

                } else {
                    //可以不做任何处理,事件的话，不是处理就是cancel，不能置之不理，太坏！
                    selectionKey.cancel();
                }
            }
        }
    }

    /**
     * 不看前面的代码，再次手写一遍
     * @param byteBuffer
     */
    private static void splitRead(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        for (int i = 0; i < byteBuffer.limit(); i++) {
            if (byteBuffer.get(i) == '\n') {
                int message_length = i + 1 - byteBuffer.position();
                ByteBuffer byteBuffer1 = ByteBuffer.allocate(message_length);

                for (int j = 0; j < message_length; j++) {
                    byteBuffer1.put(byteBuffer.get());
                }
                byteBuffer1.flip();
                String message = StandardCharsets.UTF_8.decode(byteBuffer1).toString();
                System.out.println(message);
                byteBuffer1.clear();
            }
        }

        byteBuffer.compact();
    }


    /**
     * 这个东西呢，就是非阻塞的channel，让CPU一只空转也不好啊，所以说就得用到selector
     * @param args
     * @throws IOException
     */
    public static void main2(String[] args) throws IOException {
        //使用 nio 工具类 来理解阻塞编程
        //1.创建一个Selector
        Selector selector = Selector.open();

        //2.创建一个服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //一句话改为非阻塞模式，下面的accept，就会不阻塞啦，没有连接的话就是返回null
        serverSocketChannel.configureBlocking(false);
        //绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(8080));

        //将channel给注册到selector上[将channel与selector建立联系]      Ops:0就是不关注任何事件
        //这一步呢，这个selector中会创建一个集合，专门用于保存SelectionKey，
        //这一步过后，这个集合里面就拥有了一个ServerSocketChannel的SelectionKey
        SelectionKey serverSocketSelectionKey = serverSocketChannel.register(selector, 0, null);
        // SelectionKey 就是将来可以通过这个key获得哪个channel发生的事件
        //  事件有四种类型[accept,connect,read,write]
        //      -- accept 会在有连接请求时触发
        //      -- connect 是客户端，连接后触发的
        //      -- read 可读事件
        //      -- write 可写时间
        //正是因为有这么多的事件，从设计的角度上来看selectionKey还要支持对感兴趣的事件进行订阅与监听
        serverSocketSelectionKey.interestOps(SelectionKey.OP_ACCEPT);

        //接受连接与数据的读写
        while (true) {
            //3.select方法，同样是阻塞的，如果有事件发生，那么线程才会恢复运行
            selector.select();

            //4.处理感兴趣的事件,通过selector.selectedKeys()内部包含了所有发生的事件；如果selector中存在未处理的事件，则还是会疯狂循环！
            //这一步selector.selectedKeys()又会在Selector中创建一个集合，保存发生事件channel的SelectionKey
            //这个selectedKeys的集合，只会增加，并不会自动删除
            Iterator<SelectionKey> selectionKeyIterable = selector.selectedKeys().iterator();
            while (selectionKeyIterable.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterable.next();
                //拿到SelectionKey就给它干掉，否则下次遍历，还是会遍历到，由于下面的逻辑会将事件消费掉，因此会引起事件获取为null的情况
                selectionKeyIterable.remove();

                //5.区分下事件类型
                if (selectionKey.isAcceptable()) {//如果是Accept事件
                    System.out.println("key:" + selectionKey);
                    //进行selector中注册的channel中产生的selector监听的感兴趣的处理
                    ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) selectionKey.channel();
                    //下面这一步serverSocketChannel.accept()会将，selectedKeys中的accept事件标记为已处理（就将事件删掉了，
                    //但是SelectorKeys中对应的key没有被删掉，下次iteration的时候，会引起异常，因此一定要在获取SelectionKey之后删掉！
                    //就是上面的selectionKeyIterable.remove()）
                    SocketChannel socketChannel = serverSocketChannel1.accept();
                    //进行一些读取事件的处理
                    socketChannel.configureBlocking(false);
                    //虽然取消了blocking，但是为了防止他一直做无用功，那么也将它注册到selector中
                    //这一步将SocketChannel也注册到Selector上了，也就是放到了保存SelectionKey的集合中了
                    SelectionKey clientSocketSelectionKey = socketChannel.register(selector, 0, null);
                    //让他关注可读事件
                    clientSocketSelectionKey.interestOps(SelectionKey.OP_READ);
                    System.out.println("connected socket channel:" + socketChannel);

                } else if (selectionKey.isReadable()) { //如果是read事件
                    try {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//                      socketChannel.configureBlocking(false);
                        int read = socketChannel.read(byteBuffer);
                        if (read == -1) { //如果未读取到数据，甭管是client正常关闭还是就没有数据发送来，就直接cancel掉！
                            selectionKey.cancel();
                        } else {
                            byteBuffer.flip();
                            String message = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                            System.out.println(message);
                            byteBuffer.clear();
                        }
                    } catch (IOException e) {
                        //客户端在强制关闭连接的时候，上面的socketChannel.read()读不到数据，会抛错误
                        //并且会触发channel中的read的事件，需要将事件进行取消！就不会不停的循环
                        selectionKey.cancel();
                        e.printStackTrace();
                    }

                } else {
                    //可以不做任何处理,事件的话，不是处理就是cancel，不能置之不理，太坏！
                    selectionKey.cancel();
                }

            }

        }



    }

    /**
     * 这个东西呢，就是非阻塞的channel，让CPU一只空转也不好啊，所以说就得用到selector
     * @param args
     * @throws IOException
     */
    public static void main1(String[] args) throws IOException {
        //使用 nio 工具类 来理解阻塞编程
        //1.创建一个服务器
        ServerSocketChannel serverSocketChannel = null;
        serverSocketChannel = ServerSocketChannel.open();
        //一句话改为非阻塞模式，下面的accept，就会不阻塞啦，没有连接的话就是返回null
        serverSocketChannel.configureBlocking(false);

        //2.绑定端口号
        serverSocketChannel.bind(new InetSocketAddress(8080));

        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        List<SocketChannel> socketChannelList = new ArrayList<>();
        //3.接受连接与数据的读写
        while (true) {

            //连接后产生了一个SocketChannel对象,accept()是一个阻塞方法
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                System.out.println("connecting...");
                //将对应的socketChannel放入一个全局的channelList中
                socketChannel.configureBlocking(false); //这样的话channel.read()就不会阻塞啦！,如果没有读到数据那么就返回0
                socketChannelList.add(socketChannel);
                System.out.println("connected..."+ socketChannel);
            }
            //4.接受客户端发送的数据
            for (SocketChannel channel : socketChannelList) {
                int read = channel.read(byteBuffer);
                if (read > 0) {
                    System.out.println("before read...");
                    //read()方法默认也是阻塞的

                    byteBuffer.flip();
                    String message = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                    System.out.println(message);

                    byteBuffer.clear();
                    System.out.println("after read...");
                }
            }
        }
    }

}
