# Musician Club Chat Service

想做一个基佬聊天软件

学习网络编程的第一步，从NIO开始

bytebuffer包

​		TestByteBuffer

​		--	与FileChannel配合，使用ByteBuffer读取文件中的数据；

​		TestByteBufferSplit

​		--	学习半包、粘包方案[通过分割符***\n***进行数据的处理]

​		TestByteBufferString

​		--	学习ByteBuffer转String，String转ByteBuffer的基操

​		TestFileChannel

​		--	学习使用FileChannel进行文件的复制，传说中的zere-copy喔，2G以上文件的传输方案也有

​		TestFiles

​		--	学习Files工具类的使用，练习包括：文件夹遍历、文件夹的遍历删除、文件夹内容复制

​		TestGatheringWrite

​		--	学习ByteBuffer的一次性写入，很简单

​		TestPath

​		--	学习Paths工具类的使用，结合着Files进行练习的，练习了一些创建文件夹、文件复制、删除文件

​		TestScatteringRead

​		--	学习使用ByteBuffer进行分散读，很简单



netserver包

​		TestServer

​		--	1.使用未配置的ServerSocketChannel进行BIO的理解与练习，这会产生效率，与开发难度上的问题；

​		--	2.同样使用ServerSocketChannel但设置了Blocking为false，这种则会产生CPU空转，造成资源的浪费；

​		--	3.在上一步的基础上加入大家常说的Selector选择器，将Channel注册到Selector上，进行感兴趣事件的监

​		--		听减少不必要的浪费，同时理解SelectionKeys和SelectedKeys"集合"的用意，还使用上面的

​		--		TestByteBufferSplit进行半包和粘包的处理

​		TestClient

​		--	与TestServer配合使用进行Server端相关处理的学习与练习