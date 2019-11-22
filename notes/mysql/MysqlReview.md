* [一、MyISAM,InnoDB的区别](#一MyISAM,InnoDB的区别)
* [二、如何进行SQL优化](#二如何进行SQL优化)
* [三、BTree&B+Tree](#三BTree&B+Tree)
* [四、聚集索引VS非聚集索引](#四聚集索引VS非聚集索引)
* [五、MVCC](#五MVCC)
* [六、事务性质](#六事务性质)
* [七、事务隔离级别](#七事务隔离级别)


# 一、MyISAM,InnoDB的区别

MyISAM：不支持事务，查询、插入为主的数据，不支持外键，仅支持表锁，清空表时，直接新建

    更新一个字段，就会锁整表，所以不适合大量写操作的常见

InnoDB：支持事务，外键，锁力度支持MVCC，不支持全文索引，不保存行数，清空表时，一行行删除，支持表锁、行锁


# 二、如何进行SQL优化

选择正确的存储引擎

优化字段的数据类型（越小越快）

    数据库存储单位是页，一页能存储的东西越多越好（InnoDB默认16K）
    
为搜索字段添加索引

避免select *

尽可能使用NOT NULL

    NULL需要额外的空间，并且在进行比较的时候，程序会复杂

固定长度的表会更快

    固定长度，mysql可以轻松的计算下一个数据的偏移量，副作用就是定长浪费空间


# 三、BTree&B+Tree

B+树非叶子节点不存储数据(B数存储)，仅存储键值，这样会存储更多的键值，减少IO

因为B+所有数据存储在叶子结点，数据是顺序排好的，范围查找，排序查找，分组以及去重变的更简单

MyISAM，B+树索引的叶子节点并不存储数据，而是存储数据的文件地址

通过图可以看出，数据页之间通过双向链表以及叶子节点数据通过单向链表找到所有数据

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/mysql/BTree.png" width="600px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/mysql/BPlusTree.png" width="600px">
</div>

# 四、聚集索引VS非聚集索引

聚集索引：以主键作为B+树索引的键值而构造的B+树索引

    InnoDB表中数据都会有一个主键，即使你没创建，也会帮你创建一个隐式主键

非聚集索引：主键之外的列值作为键值构成的B+树索引

区别

    非聚集索引的叶子节点不存储表中的数据，而是存储该列对应的主键
    
    再根据主键再去聚集索引中查找，这个过程称为回表

    MyISAM中，聚集索引和非聚集索引的叶子节点都会存储数据的文件地址
    
什么情况不回表

    查询所要求的字段全部命中了索引
    select age from employee where age < 20;

# 五、MVCC

Multi-Version Concurrency Control:多版本并发控制

类比java，就是一个乐观锁的实现，解决“读-写”冲突的无锁并发控制

并发读写数据库时，读写互不阻塞，同时解决了脏读，幻读，不可重复读，但不能解决更新丢失问题

所以结合MVCC

    MVCC + 悲观锁
    MVCC + 乐观锁
    MVCC解决读写冲突，悲观锁、乐观锁解决写写冲突

适用于mysql的RC，RR隔离级别

    Read Uncommitted存在脏读，串行化是表锁，不涉及行锁
    
原理
    
    保存数据在某个时间点的快照，无论事务多长，在同一事务数据一致
    
特征

    每行数据都存在一个版本，每次数据更新都更新该版本
    
    修改时Copy出当前版本随意修改，各事物之间无干扰
    
    保存时比较版本号，成功commit，失败rollback

Read View(读视图)

    事务进行快照读操作的时候产生的读视图
    
    事务执行快照读的那一刻，会生成数据库系统当前的一个快照，记录并维护当前活跃的事务ID
    
    每个事务开启时，都会被分配一个ID，这个ID递增
    
    主要做可见性判断，可能是当前最新数据，也可能是该行记录的undo log里面的某个版本数据
    
    

InnoDB实现策略

    三个隐式字段，undo日志，read view等完成
    
    三个隐式字段：
        DB_TRX_ID：最近修改（修改/插入）事务ID
        DB_ROLL_PTR：回滚指针，指向这条记录的上一个版本（配合undo日志）
        DB_ROW_ID：隐含的自增ID（没有主键时会生成）
        
    undo日志分为两种：
        insert：事务回滚时需要，事务提交可以被立即丢弃
        update：update或delete时产生，不仅在回滚时需要，在快照读时也需要
                不能删除，只要在快照读或事物回滚不涉及时，才会被purge线程统一清除

流程

    事务一：修改数据，数据库先对该行加排它锁
            把旧数据copy到undo log中，修改数据，并将隐藏字段的事务ID置为当前事务
            回滚指针指向undo log的副本记录
            事务提交，释放锁
    事务二：做同样的操作，只是最新的旧数据作为链表头
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/mysql/mvcc流程1.png" width="600px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/mysql/mvcc流程2.png" width="600px">
</div>    

# 六、事务性质

原子性、一致性、隔离性、持久性

# 七、事务隔离级别

read-uncommitted：读未提交

read-committed：读已提交（解决脏读）

repeatable-read：可重复读（解决脏读，不可重复读），mysql默认

serializable：串行化（解决脏读，不可重复读，幻读）

    脏读：一个事务处理过程中读取另一个事务未提交的数据
    
    不可重复读：一个事务范围多次查询返回不同的结果
    
    幻读：批量更新所有数据时，另一个事务添加了一条，就会产生以为有一条没有成功











