* [一、2PC与3PC](#一2PC与3PC)




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
