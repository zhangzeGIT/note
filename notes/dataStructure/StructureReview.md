* [一、布隆过滤器](#一布隆过滤器)
* [二、Raft](#二Raft)
* [三、BTree](#三BTree)

# 布隆过滤器

实际就是一个byte数组，只能大概判断key可能存在

byte数组长这样

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/布隆过滤器1.png" width="650px">
</div>

如果key为baidu，我们经过三次hash，在指定角标中赋值为1

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/布隆过滤器2.png" width="650px">
</div>

在来一个key为tencent

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/布隆过滤器3.png" width="650px">
</div>

再来一个，经过同样的hash，判断是否都是一，如果都是，那么就说明可能存在


# 二、Raft

### 概述

共识性算法，目标是便于理解，工程易于实现。性能、可靠性、可用性方面不输于Paxos

### 节点状态

Leader：接受客户端请求，并向Follower同步日志，当日志同步到大多数节点上后告诉Follower提交日志

Follower：接受并持久化Leader同步的日志

Candidate：Leader选举过程中的临时角色

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/Raft状态转换.jpg" width="650px">
</div>

### Leader选举

#### 任期

    每一个Leader都是一个任期(term)，重新选举，term+1，Leader在整个term内管理集群

#### Follower->Candidate

    Leader通过heartbeat保持与Follower的联系，当一段时间没有收到Leader的heartbeat，Follower就会触发选举

    Follower将当前term+1然后转换为Candidate

#### 投票阶段
    
    Candidate首先给自己投票，再向其他服务器发送RequestVote RPC
    
    三种情况：
        ①赢得了多数的选票，成功选举为Leader
        ②收到Leader消息，表示有其他服务器已经抢先当选了Leader(包含上一个leader没有挂的情况)
        ③没有赢得多数选票，等待时间超时，再发一次选举(比如四台机器，二比二平票)

### 日志同步

Leader接受客户端请求，Leader把请求作为日志条目(Log entries)加入到它的日志中

同时，Leader并行向Follower发送AppendEntries RPC复制日志条目

大多数复制完成(可以commit了)，Leader将这条日志应用到它的状态机并向客户端返回执行结果

某些Follower没有成功：Leader无限重试

日志由有序编号(log index)和日志条目组成，日志条目包含它被创建时的任期号(term)

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/Raft日志保存.jpg" width="650px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/Raft日志格式.jpg" width="650px">
</div>

### 安全性

拥有最新的已提交的log entry的Follower才有资格成为Leader

Leader只能推进commit index来提交当前term的已经复制到大多数服务器上的日志，旧term日志的提交要等到提交当前term的日志来间接提交（log index 小于 commit index的日志被间接提交）。

### 网络分区

假设五台机器(两个是一个网络分区，三个是一个网络分区)

导致两个leader，如何解决？

    无需解决，两个节点的会提交失败，因为最多也就只有两个节点同步成功
    会分会给客户端失败，客户端重试就好了

当分区恢复两个节点的partition的leader自动降级为Follower，同时删除没有提交的

参考：https://zhuanlan.zhihu.com/p/32052223

# 三、BTree

### 性质

### 分裂过程







