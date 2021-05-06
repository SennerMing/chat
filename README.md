# Musician Club Chat Service

想做一个基佬聊天软件

学习网络编程的第一步，从NIO开始

## niobasic包

#### 		TestByteBuffer

​		--	与FileChannel配合，使用ByteBuffer读取文件中的数据；

#### 		TestByteBufferSplit

​		--	学习半包、粘包方案[通过分割符***\n***进行数据的处理]

#### 		TestByteBufferString

​		--	学习ByteBuffer转String，String转ByteBuffer的基操

#### 		TestFileChannel

​		--	学习使用FileChannel进行文件的复制，传说中的zere-copy喔，2G以上文件的传输方案也有

#### 		TestFiles

​		--	学习Files工具类的使用，练习包括：文件夹遍历、文件夹的遍历删除、文件夹内容复制

#### 		TestGatheringWrite

​		--	学习ByteBuffer的一次性写入，很简单

#### 		TestPath

​		--	学习Paths工具类的使用，结合着Files进行练习的，练习了一些创建文件夹、文件复制、删除文件

#### 		TestScatteringRead

​		--	学习使用ByteBuffer进行分散读，很简单



## nionet包

#### 		TestServer

​		--	1.使用未配置的ServerSocketChannel进行BIO的理解与练习，这会产生效率，与开发难度上的问题；

​		--	2.同样使用ServerSocketChannel但设置了Blocking为false，这种则会产生CPU空转，造成资源的浪费；

​		--	3.在上一步的基础上加入大家常说的Selector选择器，将Channel注册到Selector上，进行感兴趣事件的监

​		--		听减少不必要的浪费，同时理解SelectionKeys和SelectedKeys"集合"的用意，还使用上面的

​		--		TestByteBufferSplit进行半包和粘包的处理

​		--	4.进行动态扩容的学习

#### 		TestClient

​		--	与TestServer配合使用进行Server端相关处理的学习与练习

## ByteBuffer

每个Channel都需要记录可能被切分的消息，因为ByteBuffer不能被多个Channel共同使用，否则数据就乱掉了，因此需要为每个Channel维护一个独立的ByteBuffer

ByteBuffer不能太大，比如一个ByteBuffer 1MB的话，需要支持百万连接的话，就要1TB的内存，因此需要设计大小可变的自适应的ByteBuffer(Netty可以做得到，hin牛批)

​		--	一种思路是，首先分配一个比较小的ByteBuffer，例如4k，如果发现数据不够，再分配8k的ByteBuffer，				4k的ByteBuffer内容拷贝至8k中，有点事消息连续的话，容易进行处理，缺点是数据拷贝耗费性能；

​		--	另一种思路就是，使用多个ByteBuffer组成的数组，一个数组不够，把多出来的内容写入新的数组，与前

​				面的区别就是消息存储不连续解析复杂，优点是避免了拷贝引起的性能损耗

## Selector

监听是否有事件发生，方法的返回值代表有多少channel发生了事件

方法一：阻塞直至绑定事件发生

```java
int count = selector.select();
```

方法二：阻塞直至绑定事件发生，或是超时（时间单位为ms）----Netty

```java
int count = selector.select(long timeout);
```

方法三：不会阻塞，也不管有没有事件，立刻返回，自己根据返回值检查是否有事件

```java
int count = selector.selectNow();
```

### select何时不阻塞

- 事件发生时

  1.客户端发起连接请求，会触发accept事件

  2.客户端发送数据过来，客户端正常或者异常关闭时，都会触发read事件，灵台如果发送数据大于buffer缓冲区，会触发多次读取事件

  3.channel可写，会触发write事件

  4.linux下nio bug发生时

- 调用selector.wakeup()

- 调用selector.close()

- selector所在线程interrupt

### 关于多线程的优化

现在都是多核CPU，设计师要充分考虑别让多核的CPU能力浪费掉

前面的代码都只有一个选择器，没有充分利用多核CPU，如何改进？

分两组选择器

- 单线程配一个选择器，专门用来处理accpet事件

- 创建CPU核心数的线程，每一个线程配一个选择器，轮流处理read事件

  意思都在代码里面，参考nionet包下面的MultiThreadServer

#### 拿到CPU个数

- Runtime.getRuntime().availableProcessors()如果JVM是运行在docker容器下的话，因为容器不是物理隔离的，会拿到物理CPU个数，而不是容器申请时的个数
- 这个问题直到JDK10才修复，使用JVM参数UseContainerSupport配置，默认开启

## NIO和BIO

### Stream和Channel

- Stream不会自动缓冲数据，Channel会利用系统提供的发送缓冲区(sendBuffer)、接收缓冲区(receiveBuffer)（更为底层）
- Stream仅支持阻塞API，Channel同时支持阻塞、非阻塞API，网络Channel可配合着selector实现多路复用
- 二者均为全双工，即读写可以同时进行（Stream也是喔！）

### IO模型

同步阻塞、同步非阻塞、多路复用、异步阻塞（网上有种理解是异步情况下，没有阻塞的，没有异步阻塞的说法）、异步非阻塞，前三者都是同步

同步：简单理解，同一个线程进行创建并获取结果

异步：简单理解，一个线程创建，一个线程获取结果返回

当调用一次Channel.read()或者Stream.read()后，会切换至操作系统内核态来完成真正的数据读取，而读取又分为两个阶段，分别为：

- 等待数据阶段
- 复制数据阶段

参考UNIX网络编程-卷1

- 阻塞IO
- 非阻塞IO
- 多路复用
- 信号驱动
- 异步IO

## 零拷贝

### 传统方式的读写

传统方式读取文件并通过网络发送，通过Java代码RandomAccessFile

```java
File file = new File("heihei/data.txt");
RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");

byte[] byte = new byte[(int)f.length()];
file.read(buf);

Socket socket = ...;
socket.getOutputStream().write(buf);
```

1. 调用file的read()方法，系统由**用户态**切换为**内核态**，调用内核态的读能力，文件从磁盘读入**内核缓冲区**，操作系统使用DMA（Director Memory Access）来实现文件读，期间不会使用CPU

   ###### DMA可以理解为硬件单元，用来解放CPU完成文件的IO

2. 文件从**内核缓**冲区读入**用户缓冲区**(JVM中)，此时，系统由**内核态**切换为**用户态**，这个期间CPU会参与拷贝，无法利用DMA了

3. 调用write方法，这是将数据从**用户缓冲区**(byte[] buf)写入socket缓冲区，cpu会参与拷贝的

4. 接下来要向网卡充写入数据了，这项能力java同样不具备，因此又得从**用户态**切换至**内核态**，调用操作系统的写能力，使用DMA将**socket缓冲区**的数据写入网卡，不会使用户到CPU资源

可以看到传统的读写，中间环节比较多，java的IO时机不是物理设备级别的读写操作，而是缓存级别的复制，底层的真正的读写是操作系统来完成的

- 用户态与内核态的切换发生了3次，这个操作比较重量级
- 数据拷贝了共4次

### NIO的优化

通过DirectByteBuffer

- ByteBuffer.allocate(10) HeapByteBuffer 使用的还是Java的Byte数组
- ByteBuffer.allocate(10)  DirectByteBuffer 使用的就不是Java中的Byte数组，是操作系统内存，Java和操作系统都可以共同进行操作，也就是说使用这个东西之后，上面传统的读数据的**第1和第2步就合并了**，直接可以将堆外内存映射到JVM中，供访问使用，不受JVM垃圾回收影响，因此内存地址固定，有助于IO读写操作。

DirectByteBuffer减少了一次数据拷贝，用户态与内核态的切换次数内有减少

**进一步优化**（磁层采用了**Linux2.1**之后提供的sendFile方法），java中对应着两个channel调用transferTo/transferFrom方法拷贝数据

1. java调用transferTo方法后，要从java程序的**用户态**切换至**内核态**，使用DMA将数据读入**内存缓冲区**，不会使用CPU
2. 数据从**内核缓冲区**传输到**socket缓冲区**，cpu会参与拷贝
3. 最后使用DMA将**socket缓冲区**的数据**写入网卡**，不会使用CPU

可以看到

- 只发生了1次用户态与内核态的切换
- 数据拷贝了3次

进一步优化（Linux2.4）

1. Java调用transferTo方法后，要从java程序的**用户态**切换至**内核态**，使用DMA将数据读入**内核缓冲区**，不会使用CPU
2. 只会将一些Offset和length信息拷贝到**socket缓冲区**，几乎无消耗
3. 使用DMA将**内核缓冲区**的数据直接写入网卡，不会使用CPU

可以看到

- 只发生了1次用户态与内核态的切换
- 数据拷贝了2次

所谓的**0拷贝**，并不是真正的无拷贝，而是在不会拷贝重复数据到JVM内存中，0拷贝的优点有：

1. 更少的用户态与内核态的切换
2. 不利用CPU进行计算，减少CPU缓存的伪共享，主要靠DMA这么一个硬件
3. 0拷贝适合小文件的传输（占用内核缓存区大，缓冲次数多）

## AIO

AIO用来解决数据复制阶段的阻塞问题

- 同步意味着，在进行读写操作的时候，线程需要等待结果，还是相当于闲置

- 异步意味着，在进行读写操作的时候，线程不必等待结果，而是将来由操作系统通过回调的方式，由另外的线程获得结果

  ###### 异步模型需要底层操作系统（Kernel）提供支持

- ###### Windows系统通过IOCP实现了真正的异步IO

- ###### Linux系统异步IO在2.6的版本引入，但其底层实现还是使用多路复用模拟了异步IO，性能没有优势

查看aio包下的AsynchronousFileChannel的使用栗子

## Netty

Netty是一个异步的事件驱动的网络应用框架，用来快速开发可维护的高可用的网络服务和客户端

Netty中的异步所指的并不是AIO，参照IO模型中的异步

Netty对于Java网络应用框架好比Spring于JavaEE

使用到Netty进行网络通信的：

- Cassandra - nosql数据库
- Spark - 大数据分布式计算
- Hadoop - 大数据分布式存储
- RocketMQ - ali开源的消息队列
- Elasticsearch - 搜索引擎
- gRPC - rpc框架
- Dubbo - ali rpc框架
- Spring 5.x - webflux，完全抛弃tomcat，使用netty作为服务器
- Zookeeper - 分布式协调框架

### Netty与NIO

Netty基于NIO，自己使用NIO工作量大，自己使用NIO

- 需要自己构建协议
- 解决TCP传输问题，像是粘包、半包
- epoll空轮询导致的CPU占用率100%
- 对API进行增强，使之更易用，如FastThreadLocal ==> ThreadLocal，ByteBuf ==> ByteBuffer

Netty vs 其他网络应用框架

- Mina由apcahe维护，将来3.x版本可能会有较大的重构，破坏API向下兼容性，Netty的开发迭代更迅速，API更简洁、文档更全面
- 久经考验的，有16年之久，Netty版本
  - 2.x 2004
  - 3.x 2008
  - 4.x 2013
  - 5.x 已废弃（并没有明显的性能提升，维护成本高）

### HelloWorld

添加依赖，实现向服务器发送HelloWorld，并返回消息

- NioEventLoopGroup：用来对事件的轮询处理，四大事件嘛，connect、accept、read、write，一旦有事件发生就触发
- childHandler：监听到连接事件发生时，就会执行initChannel，对Channel进行初始化
- pipeline：是一个流水线，当NioEventLoopGroup中监听到有读写事件发生时，pipeline里面的处理器，会按照添加顺序去执行
- sync()：阻塞方法，知道建立连接，才会放行

### 正确观念

- 把channel理解为数据的通道
- 把msg理解为流动的数据，最开始输入是ByteBuf，但经过pipeline的加工，会变成其他类型的对象，最后输出又变成ByteBuf
- 把handler理解为数据的处理工序
  - 工序有多道，合在一起就是pipeline，pipeline负责事件发布（读、读取完成...）传播给每个handler，handler对自己感兴趣的事件进行处理（重写了相应事件的处理方法）
  - handler分inbound和outbound两类

- 把eventLoop理解为处理数据的工人
  - 工人可以管理多个channel的io操作，并且一旦工人负责了某个channel，就要负责到底（绑定）
  - 工人既可以进行io操作，也可以进行任务处理，每位工人有任务队列，队列里可以堆放多个channel的待处理任务，任务分为普通任务、定时任务
  - 工人按照pipeline顺序，依次按照handler的规划（代码）处理数据，可以为每道工序指定不同工人

### 组件

#### EventLoop

EventLoop本质是一个单线程执行器（同事维护了一个Selector），里面有run方法处理Channel上源源不断的io事件。

它的继承关系比较复杂：

- 一条线是继承自JUC的ScheduleExecutorService因此包含了线程池中所有的方法
- 另一条线是继承自Netty自己的OrderedEventExecutor
  - 提供了boolean inEventLoop(Thread thread)方法，判断一个线程是否属于此EventLoop
  - 提供了parent方法来看看自己属于哪个EventLoopGroup

#### EventLoopGroup

EventLoopGroup是一组EventLoop,Channel一般会调用EventLoopGroup的register方法来绑定其中一个EventLoop，后续这个Channel上的io事件都有此EventLoop来处理（保证了io事件处理时的线程安全）

- 继承自Netty自己的EventExecutorGroup
  - 实现了Iterable接口提供遍历EventLoop的能力
  - 另有next方法获取集合中下一个EventLoop

自定义外部EvenLoopGroup，过个EventLoopGroup会绑定一个Channel，如果某个handler执行的时间过长，可以单独创建一个DefaultEventLoopGroup，为了不影响Nio的处理

多个Handler之间执行中如何换人？

多个Handler如果是在不同的EventLoopGroup中，那么怎样进行交接班的处理的？

io.netty.channel.AbstractChannelHandlerContext#invokeChannelRead()，这个函数就可以让handler一个一个往下调用

```java
final Object m = next.pipline.touch(ObjectUtil.checkNotNull(msg,"msg"),next);
EventExecutor executor = next.executor();//返回的下一个handler的eventLoop
if(executor.inEventLoop()){//当前handler中的线程，是否和executor(下一个handler的eventLoop)是同一个线程
  next.invokeChannelRead(m)
}else{//不是，将执行的代码作为任务提交给下一个事件循环eventLoop进行处理
  executor.execute(new Runnable(){
    @Override
    public void run(){
      next.invokeChannelRead(m);
    }
  });
}
```

如果两个handler绑定的是同一个线程(EventLoop)，那么就直接调用，否则，把调用的代码，封装成一个任务对象，由下一个handler的线程来调用。

#### Channel

channel的主要作用

- close()：可以用来关闭channel
- closeFuture()：用来处理channel的关闭
  - sync方法作用是同步等待channel连接的建立
  - addListener方法是异步等待channel关闭
- pipeline()方法添加处理器
- write()方法将数据写入
- writeAndFlush()方法将数据写入并立刻刷出

#### Future & Promise

在异步处理的时候，经常用到这两个接口

首先要说明Netty中的Future与JDK中的Future同名，但是是两个接口，Netty中的Future继承自JDK的Future，而Promise又对Netty Future进行了扩展

- JDK Future只能同步等待任务结束（或成功、或失败）才能得到结果
- Netty Future 可以同步等待任务结束得到结果，也可以以异步方式得到结果，但都是要等待任务结束
- Netty Promise不仅有Netty Future的功能，而且脱离了任务独立存在，只作为两个线程间传递结果的容器

##### JDK Future、Netty Future & Promise

​	cancel：JDK Future -- 取消任务

​	isCanceled：JDK Future -- 任务是否取消

​	isDone：JDK Future -- 任务是否完成，不能区分成功还是失败

​	get：JDK Future -- 获取任务结果，阻塞等待

​	getNow：Netty Future -- 获取任务结果，非阻塞，还未产生结果时返回null

​	await：Netty Future -- 等待任务结果，如果任务失败，不会抛异常，而是通过isSuccess判断

​	sync： Netty Future -- 等待任务结束，如果任务失败，抛出异常

​	isSuccess：Netty Future -- 判断任务是否成功

​	cause：Netty Future -- 获取失败信息，非阻塞，如果没有失败，返回null

​	addListener：Netty Future -- 添加回调，异步接收结果

​	setSuccess：Netty Promise -- 设置成功结果

​	setFailure：Netty Promise -- 设置失败结果

#### Handler & Pipeline

ChannelHandler用来处理Channel上的各种事件，分为入站、出站两种，所有的ChannelHandler被连成一串就是Pipeline

- 入站处理器通常是ChannelInBoundHandlerAdapter的子类，主要用来读取客户端数据，写回结果
- 入站处理器通常是ChannelOutBoundHandlerAdapter的子类，主要对写回结果进行加工

打个比喻，每个Channel是一个产品加工的车间，Pipeline是车间中的流水线，ChannelHandler就是流水线上的各道工序，而后面要讲的ByteBuf是原材料，经过很多工序的加工：先经过一道道入站工序，再经过一道道出站工序，形成最终的产品

Netty的Pipeline会默认添加head和tail，我们调用的pipeline.addLast()是添加顺序放在head和tail之间，也可以理解为每次都添加到tail之前

入站：按照in添加顺序

出站：按照out添加逆序

#### ByteBuf

##### 直接内存 vs 堆内存

可以使用下面的代码来创建池化基于堆的ByteBuf

```java
ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer(10);
```

也可以使用下面的代码来创建池化基于直接内存的ByteBuf

```java
ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer(10);
```

- 直接内存创建和销毁的代价比较昂贵，但是读写性能比较高（少一次内存复制），适合配合池化功能一起用
- 直接内存对GC压力小，因为这部分不受JVM垃圾回收的管理，但也要注意及时主动的释放

##### 池化 vs 非池化

池化的最大意义就在于可以重用ByteBuf，池化默认开启，优点有

- 没有池化，则内次都得创建新的ByteBuf实例，这个操作对直接内存代价昂贵，就算是堆内存，也会增加GC压力
- 有了池化，则可以重用池中的ByteBuf实例，并且采用了与jemalloc类似的内存分配算法提升分配效率
- 高并发时，池化功能更节约内存，减少内存溢出的可能

池化功能是否开启，可以通过下面的系统环境变量来设置

```java
-Dio.netty.allocator.type={unpooled|pooled}
```

- 4.1以后，非Android平台默认启用池化实现，Android平台启用非池化实现
- 4.1之前，池化功能还不成熟，默认是非池化实现

扩容规则

- 如果写入后数据大小未超过512，则选择下一个16的整数倍，例如写入后大小为12，则扩容后capacity是16
- 如果写入后数据大小超过512，则选择下一个2^n，例如写入后大小为513，则扩容后capacity是2^10=1024(2^9=512已经不够用了)
- 扩容不能超过max capacity，否则会报错

##### retian & release

由于Netty中有对外内存的ByteBuf实现，堆外内存最好是手动来释放，而不是等GC来回收。

- UnpooledHeadpByteBuf使用的是JVM内存，只需等待GC回收即可

- UnpooledDirectByteBuf使用的就是直接内存了，需要特殊的方法来回收内存

- PooledByteBuf和它的子类使用了池化机制，需要更复杂的规则来回收内存

  - 回收内存的源码实现，可关注下面方法的不同实现

    protected abstract void dellocate()

Netty这里采用了引用计数法来控制回收内存，每个ByteBuf都实现了ReferenceCounted接口

- 每个ByteBuf对象的初始计数为1
- 调用release方法计数减1，如果计数为0，ByteBuf内存被回收
- 调用retain方法计数加1，标识调用者没用完之前，其他handler即使调用了release也不会造成回收
- 当计数为0时，底层内存会被回收，这时即使ByteBuf对象还在，其他各个方法均无法正常使用

##### ByteBuf优势

- 池化 - 可以重用池中的ByteBuf实例，更节约内存，减少内存溢出的可能
- 读写指针分离，不需要像ByteBuffer一样切换读写模式
- 可以自动扩容
- 支持链式调用，使用更流畅
- 很多地方体现0拷贝，例如slice、duplicate、compositeByteBuf