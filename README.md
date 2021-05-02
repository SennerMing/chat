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

异步：简单理解，同一个线程进行创建并获取结果

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