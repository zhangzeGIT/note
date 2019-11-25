* [一、基本数据类型](#一基本数据类型)
* [二、redis过期时间与删除](#二redis过期时间与删除)
* [三、redis内存淘汰机制](#三redis内存淘汰机制)
* [四、redis持久机制](#四redis持久机制)
* [五、redis事务](#五redis事务)
* [六、缓存雪崩和缓存穿透问题](#六缓存雪崩和缓存穿透问题)
* [七、redis与mysql数据一致性](#七redis与mysql数据一致性)
* [八、redis为什么这么快](#八redis为什么这么快)
* [九、redis和Memcached的区别](#九redis和Memcached的区别)
* [十、redis的应用场景](#十redis的应用场景)
* [十一、redis主从与集群](#十一redis主从与集群)


# 一、基本数据类型

### String
简单key-value类型，value不仅仅可以是String，还可以是数字
应用：常规计数
    
    set get decr incr mget
### Hash（建议使用这个，存储高效）
value存放的结构化的对象，单点登录的用户信息，以cookieID为key，30分钟缓存

    hget hset hgetall
### List
List数据结构，可以做简单的消息队列功能，例如微博的关注列表，粉丝列表
利用lrange命令，可以做Redis的分页功能，性能极佳

    lpush rpush lpop rpop lrange
### Set
不重复值的集合，可以做全局去重。为什么不用JVM自带的Set去重？
因为自己的系统一般集群部署，全局去重，太麻烦了
还可以利用交集，并集，差集等操作获取共同关注，共同喜好等功能

    sadd spop smembers sunion
    sinterstore key1 key2 key3  将交集存在key1中

### Sorted Set
多了一个权重参数score，能够按照score进行排列，可以做排行榜，取TOP N操作
也可以做延时任务，范围查找

    zadd zrange zrem zcard

# 二、redis过期时间与删除

在set key的时候，都可以给一个expire time

## 删除策略

### 定期删除
redis默认每隔100ms就随机抽取一些设置了expire time的key，如果过期，就删除
为什么随机？如果存几十万，每隔100ms就遍历所有，会给CPU带来很大负载

### 惰性删除
定期删除没有删除掉，那么除非你的系统去查一下那个key，redis才会删除

## 存在的问题？
定期没有删除，之后也没查询，就会导致大量过期key堆积在内存

# 三、redis内存淘汰机制

### 六种数据淘汰策略
#### volatile-lru 
    从已设置过期时间的数据集中挑选最近最少使用的数据淘汰

#### volatile-ttl
    从已设置过期时间的数据集中挑选将要过期的数据淘汰

#### volatile-random
    从已设置过期时间的数据集中挑选任意数据淘汰

#### allkeys-lru：最常用的
    从数据集中移除最近最少使用的key
    
#### allkeys-random
    从数据集中任意选择数据淘汰

#### no-eviction
    不删除，新写入操作会报错

# 四、redis持久机制

### 快照（RDB，默认的方式）
通过创建快照来获取存储在内存里面的数据在某个时间点上的副本

    save 900 1    #在900秒(15分钟)之后，如果至少有1个key发生变化，Redis就会自动触发BGSAVE命令创建快照。
    save 300 10   #在300秒(5分钟)之后，如果至少有10个key发生变化，Redis就会自动触发BGSAVE命令创建快照。
    save 60 10000 #在60秒(1分钟)之后，如果至少有10000个key发生变化，Redis就会自动触发BGSAVE命令创建快照。

### 追加文件（append-only-file AOF）
实时性更好，成为主流的持久化方案，默认没有开启。保存地址与RDB文件相同，都是通过dir参数设置
默认文件名appendonly.aof，为了兼顾数据和写入性能，可以考虑everysec选项

    开启：appendonly yes
    appendfsync always   #每次有数据修改发生时都会写入AOF文件,这样会严重降低Redis的速度
    appendfsync everysec #每秒钟同步一次，显示地将多个写命令同步到硬盘
    appendfsync no      #让操作系统决定何时进行同步

### 混用
redis4.0开始支持RDB和AOF的混合持久化，AOF重写直接把RDB的内容写到AOF文件开头

好处：结合两者优点，快速加载的同时避免丢失过多的数据
缺点：AOF里面的RDB部分是压缩格式，不再是AOF格式，可读性查

    aof-use-rdb-preamble
#### AOF重写
执行BGREWRITEAOF命令时，Redis服务器维护一个AOF重写缓冲区

该缓冲区会在子进程创建新AOF文件期间，记录服务器执行的所有写命令

创建完新的AOF，服务器会将重写缓冲区中的所有内容追加到新AOF文件末尾，用新的替换旧的AOF


# 五、redis事务

通过MULTI，EXEC，WATCH等命令来实现事务功能，提供将多个命令请求打包
事务执行期间，服务器不会中断事务而改去执行其他客户端的命令请求
会将事务中的所有命令都执行完毕，然后才会去处理其他客户端的命令请求

# 六、缓存雪崩和缓存穿透问题

## 缓存雪崩
缓存大面积失效，请求落在数据库上，造成数据库大量请求而崩溃

解决方案：
    
    保证redis集群高可用，机器宕机尽快补上，选择合适内存淘汰策略
    本地ehcache缓存+hystrix限流&降级
    数据预热，发生大并发访问前预先去更新缓存，并且设置不同的过期时间
    
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/redis/缓存雪崩.png" width="800px">
</div>

## 缓存击穿
故意请求缓存不存在的数据，导致所有请求都落在数据库上

解决方案：

    布隆过滤器：
    缓存空对象：查询返回空，也把这个值进行缓存，只是过期时间很短

# 七、redis与mysql数据一致性

### 造成redis与mysql数据不一致的原因

#### 先删除Redis，再更新MySQL，更新失败
例子：删除完成，还没来得及更新，这时候，来一个线程读，会发现缓存没有数据，就去MySQL中读，读到的是历史数据，并更新了缓存

解决方案：延时双删策略，并设置合理过期时间

弊端：最差情况是过期时间内数据存在不一致性，增加了写请求的耗时
#### 先写MySQL，再删除Redis，删除失败
解决方案：异步更新缓存，基于订阅binlog的同步机制
          Redis更新分为全量和增量
          读取binlog后分析，利用消息队列推送更新各台redis缓存数据


# 八、redis为什么这么快

完全基于内存
处理网络请求使用的是单线程，避免不必要的上下文切换和锁的竞争维护
使用I/O多路复用模型（react模型）

# 九、redis和Memcached的区别

Redis支持多种数据结构存储
Redis支持数据的备份，即master-slave模式
Redis支持数据的持久化

# 十、redis的应用场景

会话缓存，队列（基于list和set），排行榜/计数器

# 十一、redis主从与集群

### 主从结构
#### 原理

    ①从库和主库建立MS关系之后，会向主发送SYNC命令
    ②主接收命令，会开始在后台保存快照（RDB持久过程），并将期间快照命令缓存起来
    ③快照完成之后，主将快照和命令发送给从
    ④从载入快照并执行收到的缓存命令
    ⑤之后，redis每当接收到写命令，就会发送给redis
    
#### 宕机    
从宕机重启，增量复制，不是全量
主宕机：从执行SLAVEEOF NO ONE命令，断开主从关系提升为主
        主重启，执行SLAVEOF命令，设置为从    

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/redis/redis主从.png" width="650px">
</div>

#### 哨兵（sentinel）
独立进程
监控主从是否正常运行
主故障自动将从转为主
多个哨兵，互相监控
哨兵启动无需配置slave，只需要指定master即可，自动发现slave

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/redis/哨兵.png" width="650px">
</div>

### 集群模式
#### 简介
redis3.0支持集群，每个节点为分片，协议为gossip，每个节点都可以是一个个主从
节点fail是通过集群中超过半数的节点检测失效才生效
把所有的屋里节点映射到[0-16348]slot上，cluster每台机器维护槽位到机器的映射关系
slot计算方式CRC16(key) % 16384
client任意连接集群中的一个节点，如果数据对应的槽位不在本地，就做重定向操作

#### 故障转移
主备切换，参考上面
新主节点撤销所有对宕机节点的槽指派，并将这些槽全部指派给自己
发送广播gossip PONG消息，让其他机器知道自己是新的主

#### 新加和删除节点
添加：执行命令将节点添加到集群，再执行命令分槽
删除：将节点所有插槽转移到其他节点，在删除

    ./redis-trib.rb add-node IP:PORT 往集群中添加机器
    ./redis-trib.rb reshard IP:PORT 转移此redis的槽位
    ./redis-trib.rb del-node IP:PORT 删除节点
    
#### 缺点：
jedis不支持client先计算在归并mget结果
如果插槽数有部分没有指定到节点的，那么这部分插槽所对应的key将不能使用（主从解决这个问题）
只能使用0号数据库
推荐使用codis

#### 参考
https://blog.csdn.net/xxssyyyyssxx/article/details/72831909

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/redis/集群.png" width="650px">
</div>
    

