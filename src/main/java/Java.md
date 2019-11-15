<!-- GFM-TOC -->
* [一、概览](#一概览)
* [二、磁盘操作](#二磁盘操作)
* [三、字节操作](#三字节操作)


# 一、概览

    Java 的 I/O 大概可以分成以下几类：

    - 磁盘操作：File
    - 字节操作：InputStream 和 OutputStream
    - 字符操作：Reader 和 Writer
    - 对象操作：Serializable
    - 网络操作：Socket
    - 新的输入/输出：NIO

# 二、磁盘操作

File 类可以用于表示文件和目录的信息，但是它不表示文件的内容。

递归地列出一个目录下所有文件：