* [一、2PC与3PC](#一2PC与3PC)
* [二、常见限流算法](#二常见限流算法)



# 一、2PC与3PC

### 2PC（Two-Phase Commit）

协调者：事务发起者

参与者：事务执行者

准备阶段：发起者询问参与者是否可提交事务
          参与者执行事务操作，将Undo和Redo放入事务日志中，但不提交
          
提交阶段：所有参与者返回yes，提交事务
          任意一个参与者返回NO，事务回滚

缺点

    同步阻塞：所有参与者的事务逻辑均处于阻塞状态，即长事务
    单点：协调者存在单点问题，如果故障，可能导致参与者一直处于锁定状态
    脑裂：提交阶段只有部分接收到了commit请求，会导致数据不一致
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/distributed/2PC.png" width="400px">
</div>

### 3PC（Three-Phase Commit）

改动点：

    将事务提交过程分为三个阶段
    引入超时机制，同时在协调者和参与者中都引入超时机制

#### CanCommit
协调者只是询问参与者是否可以执行事务操作

#### PreCommit
协调者发送事务预执行请求，类似于2PC的第一个阶段

参与者收到abort请求进行回滚

参与者超时默认执行commit

#### doCommit
提交事务，参与者执行完毕发送ACK

协调者没有收到全部应答(或者超时)，执行中断事务

参与者根据阶段二记录的undo信息来执行回滚

优缺点：

    主要解决单点问题，并减少阻塞(超时)
    参与者无法及时收到来自协调者的信息(abort)，会默认执行commit，这样就会导致数据不一致

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/distributed/3PC.png" width="400px">
</div>

# 二、常见限流算法
### 计数器

比如某服务器每秒只能处理100个请求，可以设置一个1s的滑动窗口

窗口中有10个格子，每个格子100毫秒，每100毫秒移动一次，每次移动记录当前服务请求的次数

内存中保存10此，可以用LinkedList来实现

格子每次移动的时候判断一次，当前访问次数和LinkedList中最后一个相差超过100，就限流

服务访问次数可以放在redis

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/distributed/计数器.jpg" width="600px">
</div>

### 漏桶算法
此算法可以实现流量整形(Traffic Shaping)和流量控制(Traffic Policing)

一个固定容量的漏桶，按照常量固定速率流出水滴；

如果桶是空的，则不需流出水滴；

可以以任意速率流入水滴到漏桶；

如果流入水滴超出了桶的容量，则流入的水滴溢出了（被丢弃），而漏桶容量是不变的。

单机系统可以使用队列实现，分布式环境中消息中间件或者redis都是可选方案

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/distributed/漏桶算法.jpg" width="600px">
</div>

### 令牌桶算法
一个存放固定容量令牌(token)的桶，固定速率往桶里加令牌

令牌将按照固定的速率被放入令牌桶中。比如每秒放10个。

桶中最多存放b个令牌，当桶满时，新添加的令牌被丢弃或拒绝。

当一个n个字节大小的数据包到达，将从桶中删除n个令牌，接着数据包被发送到网络上。

如果桶中的令牌不足n个，则不会删除令牌，且该数据包将被限流（要么丢弃，要么缓冲区等待）。

通过放令牌的速率去控制输出的速率，也就是to network的速率(处理程序)

和漏桶算法比，可以通过放入令牌的速度，控制整体数据处理的速度

科普：

    Nginx两种限流方式，连接数限流模块和漏桶算法
        连接数限流是对每个IP限制连接数
        漏桶算法是设置平均处理请求频率的阈值
    Guava实现了平滑突发限流和平滑预热限流

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/distributed/令牌桶.jpg" width="600px">
</div>


