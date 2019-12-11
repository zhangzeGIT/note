* [一、Kafka与MQ](#一Kafka与MQ)
* [二、Kafka传递语义](#二Kafka传递语义)
* [三、如何保证消息只传递一次](#三如何保证消息只传递一次)
* [四、Kafka如何保存数据](#四Kafka如何保存数据)
* [五、Kafka保留策略](#五Kafka保留策略)
* [六、Partition如何分布在不同的broker上](#六Partition如何分布在不同的broker上)
* [七、ISR-HW-LEO](#七ISR-HW-LEO)
* [八、Kafka-Controller](#八Kafka-Controller)
* [九、Broker-Failover](#九Broker-Failover)
* [十、为什么需要ISR](#十为什么需要ISR)
* [十一、GroupCoordinator](#十一GroupCoordinator)
* [十二、ZeroCopy](#十二ZeroCopy)


# 一、Kafka与MQ

### Kafka
高吞吐，一般配合大数据类的系统来进行实时数据计算，日志采集等场景

内部采用消息的批量处理，zerocopy机制，数据存储、获取是本地磁盘顺序批量操作

通过pagecache机制，尽可能利用机器上的空闲内存做缓存

单机TPS约百万条/秒(10字节)，producer端将多个小消息合并，批量发送Broker

kafka消费失败不支持重试

kafka按照offset回溯消息

kafka并行度与分区数一致

kafka是消费者主动拉取的方式

### RocketMQ

rocketMQ消费失败支持定时重试，每次重试间隔时间顺延(例如充值，过一会可能就好了)

支持定时消息，支持顺序消息，支持分布式事务消息，支持MessageID查询消息，支持tag方式过滤

rocketMQ支持按时间回溯消息

rocketMQ的顺序消息并行度同Kafka，乱序消息取决于Consumer的线程数

单机TPS约7万条/秒(10字节)


# 二、Kafka传递语义
## producer端
producer端的参数，request.required.acks的配置
一条消息一旦commit，由于副本机制，就不会丢失
### At most once：0
消息可能丢，但绝对不会重复传递

producer不等待来自broker同步确认，继续发送下一批数据
### At least once：1
消息绝对不会丢，但可能会重复传递

producer在leader已成功收到数据并接收到ACK时，发送下一条

由于网络等原因，没有收到ACK，producer就会重复生产

### Exactly once：-1
每条消息只会被传递一次

所有副本同步成功，broker才会返回成功

## consumer端

### 读取消息->提交offset->处理消息：At most once
保存offset成功，处理失败，下次消费从新的offset开始

### 度取消息->处理消息->提交offset：At least once
consumer挂了，导致提交offset失败，导致下次消费还是从以前的offset开始

# 三、如何保证消息只传递一次

# Exactly Once语义
生产者不重复生产
消费者不重复拉取

# producer

正常不会重复生产，但遇到网络问题，无法收到ACK，可能重传，导致At least once

方案：

    ①每个分区一个生产者，出现异常，查询分区的最后一个消息，决定后续操作
    ②为消息添加一个全局唯一主键，消费者去重
    
# consumer

方案：
    
    ①consumer关闭自动提交且不手动提交，自己保存offset
    ②保存offset(保存MySQL中)和业务逻辑在一个事务中
    ③consumer宕机或者Rebalance时，consume从MySQL中找到offset，调用KafkaCosumer.seek方法设置offset
    注：可以通过向KafkaConsumer添加ConsumerRebalanceListener来监听Rebalance


# 四、Kafka如何保存数据
通过log.dirs配置保存的路径，可以是多个

每个partition对应目录下的一个文件夹，名称：Topic名+分区ID

分区在逻辑上对应一个Log，Log由多个segment组成，每个segment对应一个日志文件(xxx.index)，一个索引文件(xxx.log)

索引文件为稀疏索引，运行时加载到内存

日志文件超过一定大小(默认1G)，就会创建新的segment，命名为起始的偏移量

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/kafka/kafka目录结构.png" width="600px">
</div>

# 五、Kafka保留策略

### 基于时间
log.retention.hours=168

### 基于大小
log.retention.bytes=1073741824

### 基于起始位移
主要解决流处理应用中存在大量的中间消息

### 注

    log.retention.check.interval.ms：设置每次触发定时任务的周期，默认五分钟
    以上配置可以基于全局的，也可以基于topic的

# 六、Partition如何分布在不同的broker上
顺序分配，轮训

    int i = 0;
    list{kafka01,kafka02,kafka03}
    
    for (int i = 0; i < 5; i++) {
        brIndex = i % broker;
        hostName = list.get(brIndex);
    }

# 七、ISR-HW-LEO
### ISR(In-Sync Replica)

定义：

    目前可用且消息量与leader差不多的副本集合
    每个leader副本维护这个集合
条件：

    1、副本所在节点必须维持着zk的链接
    2、副本最后一条消息的offset与leader最后一条消息的offset之间不能超过指定阈值
    
### HW(HighWatermark)
消费者只能拉取HW之前的消息，由leader副本管理

当ISR集合中全部follower副本都拉取HW指定消息进行同步，leader副本递增HW

kafka将HW之前的消息状态称为“commit”

### LEO(Log End Offset)
追加到当前副本的最后一个消息，所有副本维护

生产者向leader追加消息，leader副本LEO递增

follower从leader副本拉取消息并更新到本地的时候，follower的LEO递增


# 八、Kafka-Controller

### 选举：

    broker在ZK中创建/controller临时节点：{"version":1,"brokerid":0,"timestamp":"123"}
    只有一个节点会竞选成功，每个broker都会保存当前controller的brokerID
    
    /controller_epoch记录当前控制器是第几代，初始值为1
    与controller的交互都会携带这个字段，如果epoch值小于内存中的值，就认为是过期请求
    
### 职责

#### 监听partition的变化
    为Zookeeper中的/admin/reassign_partitions节点注册PartitionReassignmentListener，用来处理分区重分配的动作。
    为Zookeeper中的/isr_change_notification节点注册IsrChangeNotificetionListener，用来处理ISR集合变更的动作。
    为Zookeeper中的/admin/preferred-replica-election节点添加PreferredReplicaElectionListener，用来处理优先副本的选举动作。
    为Zookeeper中的/brokers/topics/[topic]节点添加PartitionModificationsListener，用来监听topic中的分区分配变化。
#### 监听topic相关的变化

    为Zookeeper中的/brokers/topics节点添加TopicChangeListener，用来处理topic增减的变化；
    为Zookeeper中的/admin/delete_topics节点添加TopicDeletionListener，用来处理删除topic的动作。

#### 监听broker相关的变化

    为Zookeeper中的/brokers/ids/节点添加BrokerChangeListener，用来处理broker增减的变化。
#### 管理集群

    启动并管理分区状态机和副本状态机
    更新集群的元数据信息
    参数auto.leader.rebalance.enable=true，会开启一个“auto-leader-rebalance-task”的定时任务来负责维护分区的优先副本的均衡
    
### 优点
在没有采用controller来管理分区和副本状态时，所有操作都依赖于ZK

broker会在ZK上注册大量的Watcher，这种设计会有脑裂、羊群效应以及ZK过载等隐患

# 九、Broker-Failover

### 注意：

    新加入broker，kafka什么都不做，只会在之后创建topic的时候用上，也可以手动reassignment
    非leader replica挂了，也不会新建replica，即坏一个少一个
    
    这样会导致一个问题，当有一个broker挂了，这个broker在启动时，就没有leader replica了
    就会导致负载不均衡。可以通过auto.leader.rebalance.enable: true启动一个scheduler线程(controller启动)
    定期去为每个broker做rebalance(条件为：imbalance ratio 达到一定比例)
    
#### ①controller通过watcher(/brokers/ids/[brokerId])得知broker宕机

#### ②controller从/brokers/ids节点下读取可用broker

#### ③controller获取set_p，该集合包含宕机broker的所有partition

#### ④对每一个partition

    从/brokers/topics/[topic]/partitions/[partition]/state节点读取ISR
    决定新的leader
    将leader，isr，controller_epoch和leader_epoch写入state节点

#### ⑤通过RPC向相关broker发送leaderAndISRRequest命令

# 十、为什么需要ISR

## 分布式冗余备份的两种手段

### 同步复制

所有follower都复制成功，才算提交成功

缺点：慢的follower会拖慢整个系统，同时follower故障导致HW无法完成递增

### 异步复制

leader副本收到消息就认为消息提交成功

缺点：follower副本永远落后leader，从新leader选举会导致数据丢失

### ISR
kafka权衡了两种策略，引入ISR，解决上面两种问题

如何解决：
    
    follower副本延迟高，leader副本将其踢出ISR，避免高延迟的拖累整个集群性能
    
    leader挂了，优先从ISR集合中选举leader副本，新的leader包含HW之前的所有消息

# 十一、GroupCoordinator
### 作用
0.9版本之前，consumer的Rebalance通过ZK实现

为避免Rebalance存在不可避免的羊群效应和脑裂问题，kafka设计实现了Coordinator

Coordinator提供对group成员的Rebalance及offset管理

    维持group的成员组成，协调group成员的行为

### 选举
对于每一个ConsumerGroup，kafka为其从broker集群中选择一个broker作为其coordinator

该分区leader所在的broker就是被选定的coordinator

### 处理的请求类型

  ApiKeys.OFFSET_COMMIT;
  ApiKeys.OFFSET_FETCH;
  ApiKeys.JOIN_GROUP;
  ApiKeys.LEAVE_GROUP;
  ApiKeys.SYNC_GROUP;
  ApiKeys.DESCRIBE_GROUPS;
  ApiKeys.LIST_GROUPS;
  ApiKeys.HEARTBEAT;

### offset管理

offset位移管理

    位移保存在_consumers_offsets的topic中
    partition = Math.abs(groupId.hashCode) % groupMetaDataTopicPartitionCount
    groupMetaDataTopicPartitionCount由offsets.topic.num.partitions指定，默认五十个分区

offset commit

    消费端一条offset提交消息会作为生产请求
    broker端会处理这个请求
    
offset fetch

    消费端连接任意存活的brokers，发送OffsetFetchRequest，包含多个topic-partitions
    当前broker会寻找leader partition，进行请求转发，接收到请求的broker在offset manager中读取出offset
    
### Consumer Rebalance
每次Rebalance之后，generation号都会加一
#### ①Group寻找GroupCoordinator

    会向集群中任意broker发送GroupCoordinatorRequest，处理并返回GroupCoordinatorResponse
    该partition leader所在的broker就是该group对应的GroupCoordinator

#### ②找到之后，发送JoinGroup

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/kafka/JoinGroup.png" width="500px">
</div>

#### ③JoinGroup返回之后，发送SyncGroup，得到自己所分配的partition

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/kafka/SyncGroup.png" width="500px">
</div>

#### 注

    partition分配策略有client决定，第二步，coordinator会指定一个consumer作为leader
    有leader进行partition的分配，leader通过SyncGroup消息，将分配结果发给coordinator
    其他consumer也发送SyncGroup消息，获取这个分配结果


# 十二、ZeroCopy

日志存储分为多个FileMessage，当一个FileMessage的最大offset已经满足不了消费者时，直接返回，主要目的就是实现ZeroCopy

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/kafka/ZeroCopy.png" width="700px">
</div>






