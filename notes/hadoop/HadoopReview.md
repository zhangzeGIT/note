* [一、为什么需要MapReduce](#一为什么需要MapReduce)
* [二、hadoop启动那些进程](#二hadoop启动那些进程)
* [三、hadoop几个默认端口含义](#三hadoop几个默认端口含义)
* [四、HDFS写入流程](#四HDFS写入流程)
* [五、HDFS读取流程](#五HDFS读取流程)
* [六、NameNode和SecondaryNameNode](#六NameNode和SecondaryNameNode)


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








