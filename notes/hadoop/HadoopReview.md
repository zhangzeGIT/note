* [一、为什么需要MapReduce](#一为什么需要MapReduce)
* [二、hadoop启动那些进程](#二hadoop启动那些进程)
* [三、hadoop几个默认端口含义](#三hadoop几个默认端口含义)
* [四、HDFS写入流程](#四HDFS写入流程)
* [五、HDFS读取流程](#五HDFS读取流程)
* [六、NameNode和SecondaryNameNode](#六NameNode和SecondaryNameNode)
* [七、NameNode的安全模式](#七NameNode的安全模式)
* [八、shuffle过程](#八shuffle过程)
* [九、MapReduce过程中怎么解决很多小文件问题](#九MapReduce过程中怎么解决很多小文件问题)
* [十、Hadoop提交任务过程](#十Hadoop提交任务过程)



# 一、为什么需要MapReduce
MapReduce是分布式框架

海量数据在单机上处理因为硬件资源限制，无法胜任

单机程序如果扩展为分布式集群运行，将极大增加程序的复杂度和开发难度

引入MapReduce，开发人员可以将绝大部分工作集中在业务逻辑开发中


# 二、hadoop启动那些进程

#### NameNode
管理文件系统元数据

#### DataNode
存储数据节点

#### SecondaryNameNode
帮助NameNode合并镜像文件和编辑日志

#### ResourceManager
调度DataNode上的工作

#### NodeManager
指定任务

# 三、hadoop几个默认端口含义

dfs.namenode.http-address:50070          web查看HDFS文件系统
yarn.resourcemanager.webapp.address:8088 web端查看yarn


# 四、HDFS写入流程

#### 客户端请求NameNode是否可上传，并返回客户端是否可以上传
    
    文件存在或者目录不存在，都不可以上传

#### 客户端请求上传第一个block， NameNode返回一个DataNode列表（副本数）

    DataNode选举策略：可配置机架感知配置
        1、先考虑客户端最近的机器（同一个机架上）
        2、第二个副本考虑距离远的（不同机架）
        3、第三个副本就在第一个副本机架另外挑选一台DataNode
        
#### 客户端与DataNode建立block传输通道(第一个副本)，第一个副本与第二个建立通道……

#### 上一步成功之后，客户端一个个packet(64K)发送给第一个DataNode，第一个发送给第二个……

#### 第二个block重复，NameNode记录下来文件存储路径，有几个副本等

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/hdfs写数据.png" width="450px">
</div>

# 五、HDFS读取流程

#### 客户端向NameNode请求下载文件，NameNode返回文件所有block及路径元数据

#### 客户端通过元数据，查找block1所在最近的机器，建立连接，发送数据

#### 传输完成，继续查找block2所在的最近的机器……

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/hdfs读数据.png" width="450px">
</div>

# 六、NameNode和SecondaryNameNode

#### NameNode
元数据管理，存储在内存，内存中数据定时存在fsimage中（namesecondary目录）
一条元数据平均150byte，上亿条无法很好dump成fsimage，10亿个10几个G

#### SecondaryNameNode
fsimage镜像文件管理

管理流程：
1、NameNode首先在内存中更新元数据，再记录操作日志(工作目录下的edits_inprogress日志)

2、请求NameNode是否checkpoint(定时或者日志中的记录数据量)

3、checkpoint：合并日志文件与fsimage成一个新的fsimage，上传到NameNode覆盖老的

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/checkpoint.png" width="800px">
</div>

# 七、NameNode的安全模式

NameNode刚启动的时候，内存中只有文件和块的ID及副本数量（fsimage里就这些），不知道块所在的DataNode

NameNode等待DataNode汇报自身持有的块信息，NameNode补全元数据

当NameNode找到99.8%的信息才会对外提供服务


# 八、shuffle过程

### map端

##### ①分区Partition
map函数处理结束之后，进行分区操作（指定给哪个reducer），默认HashPartitioner(按照key)

##### ②写入环形缓冲区
环形缓冲区默认大小100M，主要保存中间结果，避免频繁IO

    mapreduce.task.io.sort.mb调整环形缓冲区
    
##### ③缓冲区溢出，排序(快排)
达到阈值(缓冲区的80%)，锁定这部分内存，对每个分区进行排序(按照partition和key两个关键字排序)
剩下的20%继续写map输出的键值对

    mapreduce.map.io.sort.spill.percent,默认0.80

##### ④Combiner(相当于map阶段的reduce)
合并，可选阶段，分区排序之后，溢写之前调用，将相同key的value值相加
好处就是减少溢出到磁盘的数据量

两个地方会调用
1、缓存溢出写文件时调用
2、缓存溢出文件数量超过指定值时，缓存文件合并时调用

    mapreduce.map.combine.minspills（默认3）时

##### ⑤写（spill）到本地磁盘
将上面的结果，写入磁盘
    
    写入到此目录：mapreduce.cluster.local.dir

##### ⑥Merge归并
将第五步产生的"多个spill"文件归并成"一个"已分区且已排序的大文件
这个过程包括排序和Combiner(可选)
归并完成，删除所有临时溢出文件

### reduce端
##### ①复制copy
Reduce启动一些数据copy线程，HTTP请求MapTask所在的NodeManager获取文件
只要有一个map任务完成，reduce任务就开始复制其输出(默认线程数为5)
reduce通过partition知道自己处理的数据
map任务完成后，使用心跳机制通知application master，master知道map输出和主机位置之间的映射
reduce中一个线程定期询问master以便获取map输出主机的位置，直到获取所有输出位置

    mapreduce.reduce.shuffle.parallelcopies属性进行设置线程数
    
##### ②Merge归并(归并排序)
Copy过来的数据线放入内存缓冲区中(默认JVM heap size的70%)

如果内存放得下所有数据那么就直接在内存中merge

如果内存放不下，当达到阈值时(66%)，进行内存到磁盘的merge
merge合并写入磁盘之前，如果设置了Combiner，也会执行

当map输出全部copy完成，reducer上会有多个文件，这时开始磁盘到磁盘的merge(合并文件)
最终输出一个整体有序的数据库

    mapred.job.shuffle.input.buffer.percent 配置，默认是JVM的heap size的70%
    mapred.job.shuffle.merge.percent        配置，默认是66%

##### reduce
key相同的是一个组，一个组的value会组成value迭代器传入reduce函数
job.setGroupingComparatorClass设置分组函数

    设置reduceTask计算时的内存buffer，默认0，如果文件很小，可以设置成基于内存的计算
    mapreduce.reduce.input.buffer.percent
        

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/shuffle官网图.png" width="800px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/MapReduce1.png" width="800px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/MapReduce2.png" width="800px">
</div>

# 九、MapReduce过程中怎么解决很多小文件问题

默认不足128M，一个文件一个切片，一个切片一个MapTask
如果压缩文件不可分割，那么一个压缩文件是一个切片

策略1：前端处理，将小文件合并成大文件，再上传到HDFS
策略2：使用CombinerFileInputFormat做文件切片(默认是TextInputFormat)

     //尽量实现最小需求，但一定不会超过最大的
    CombineTextInputFormat.setMaxInputSplitSize(job, 4194304);  //4M 最好是128，因为hadoop已经测试了，这个大小运行速率很高
    CombineTextInputFormat.setMinInputSplitSize(job, 2097152);

# 十、Hadoop提交任务过程
### 涉及的节点
本地提交任务客户端
ResourceManage
NodeManage

##### 客户端向ResourceManage申请提交一个application
客户端通过yarnrunner，yarnrunner保存了与ResourceManager之间的链接

##### ResourceManager返回Application资源路径(hdfs://xxx../staging)及Application.id

##### 客户端提交所需资源文件到资源路径下

    hdfs://xxx../staging/application.id/job.split
    hdfs://xxx../staging/application.id/job.xml
    hdfs://xxx../staging/application.id/wordcount.jar
    这些文件都是job.submit()生成的

##### 客户端通知ResourceManager提交完毕，申请运行mrAppMaster

##### ResourceManager收到请求，将客户端请求封装成task，放入FIFO队列

##### NodeManager领取任务，创建容器(Container)，下载job资源到本地，运行mrAppMaster

    mrAppMaster是整个任务的主管
    
##### mrAppMaster向ResourceManager申请运行mapTask的容器

    会发送jar包到指定容器（默认名字YarnChild）
    如果有一个MapTask运行失败或者特别慢，会新申请一个容器运行
    
##### MapTask运行完，mrAppMaster向ResourceManager申请运行ReduceTask的容器

    默认名字也是YarnChild
##### ReduceTask向MapTask获取响应分区的数据，并运行

##### 运行完，所有容器会向ResourceManager申请注销自己

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/hadoop/hadoop提交任务.png" width="800px">
</div>