# Musician Club Chat Service

想做一个基佬聊天软件

学习网络编程的第一步，从NIO开始

## bytebuffer包

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



## netserver包

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



