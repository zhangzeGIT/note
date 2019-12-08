* [一、Kafka与MQ](一、Kafka与MQ)
* [二、Kafka传递语义](二Kafka传递语义)
* [三、如何保证消息只传递一次](三如何保证消息只传递一次)




# 一、Kafka与MQ

### Kafka
高吞吐，一般配合大数据类的系统来进行实时数据计算，日志采集等场景

内部采用消息的批量处理，zerocopy机制，数据存储、获取是本地磁盘顺序批量操作

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

多数副本同步成功，broker才会返回成功

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










