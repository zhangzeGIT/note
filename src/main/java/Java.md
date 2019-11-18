<!-- GFM-TOC -->
* [一、CAP](#一CAP)
* [二、磁盘操作](#二磁盘操作)
    * [IO](#IO)
    * [HH](#HH)
* [三、图片的插入方式](#三图片的插入方式)


# 一、CAP

    C：Consistency         一致性
    A：Availability        可用性
    P：Partition tolerance 分区容错性
        分布式系统在遇到任何网络分区故障的时候，仍能保证对外提供满足一致性和可用性的服务，除非整个网络都发生了故障。

# 二、磁盘操作
 ## IO
    hhh
 ## HH
    DDDDDD
    DDDD
    DDDDDD
    DDDDD
    
    AQS
    BASE
    CountDownLatch
    CyclicBarrier  赛克雷克：环的，循环的  拜瑞尔：屏障
    Semaphore      赛么for：信号量
    parallel       派瑞拉：平行线
    ParNew GC      开行GC

File 类可以用于表示文件和目录的信息，但是它不表示文件的内容。

递归地列出一个目录下所有文件：

# 三、图片的插入方式

```java
FileInputStream fileInputStream = new FileInputStream(filePath);
BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
```

![Image](https://github.com/zhangzeGIT/note/blob/master/assets/1.png)

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/1.png" width="200px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/张泽.png" width="400px">
</div>

