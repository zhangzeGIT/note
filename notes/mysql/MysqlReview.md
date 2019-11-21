* [一、MyISAM,InnoDB的区别](#一MyISAM,InnoDB的区别)
* [二、如何进行SQL优化](#二如何进行SQL优化)
* [三、BTree&B+Tree](#三BTree&B+Tree)
* [四、聚集索引VS非聚集索引](#四聚集索引VS非聚集索引)
* [五、TCP为什么安全可靠](#五TCP为什么安全可靠)
* [六、TCP流量控制](#六TCP流量控制)
* [七、HTTP和HTTPS的区别](#七HTTP和HTTPS的区别)
* [八、SSL握手](#八SSL握手)
* [九、CA证书](#九CA证书)


# 一、MyISAM,InnoDB的区别

MyISAM：不支持事务，查询、插入为主的数据，不支持外键，仅支持表锁，清空表时，直接新建

    更新一个字段，就会锁整表，所以不适合大量写操作的常见

InnoDB：支持事务，不支持全文索引，不保存行数，清空表时，一行行删除，支持表锁、行锁


# 二、如何进行SQL优化

选择正确的存储引擎

优化字段的数据类型（越小越快）

    数据库存储单位是页，一页能存储的东西越多越好（InnoDB默认16K）

# 三、BTree&B+Tree

B+树非叶子节点不存储数据，仅存储键值，这样会存储更多的键值，减少IO

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







