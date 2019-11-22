* [一、虚拟机](#一虚拟机)
    * [1.1、内存模型](##1.1内存模型)
    * [1.2、判断对象是否存活的算法](##1.2判断对象是否存活的算法)
    * [1.3、引用类型](##1.3引用类型)
    * [1.4、垃圾回收算法](#1.4垃圾回收算法)
    * [1.5、新生代老年代内存如何回收](#1.5新生代老年代内存如何回收)
    * [1.6、垃圾回收器有哪些](#1.6垃圾回收器有哪些)
    * [1.7、常用的JDK命令行工具](#1.7常用的JDK命令行工具)
    * [1.8、类加载器分类](#1.8类加载器分类)
* [二、多线程](#二多线程)
* [三、基础知识](#三基础知识)
    * [3.1、JVM&JRE&JDK](##3.1JVM&JRE&JDK)
    



# 一、虚拟机

## 1.1、内存模型

本地方法栈：为虚拟机使用到的Native方法服务，其他同虚拟机栈，HotSpot将二者合为一

虚拟机栈：线程私有，每个方法在执行的同时创建一个栈帧用于存储局部变量，操作数栈，动态链接，方法出口等
          此区域异常StackOverflowError,OutOfMemoryError

程序计数器：线程私有，改变计数器值获取下一条需要的字节码指令

方法区：线程共享，存储类信息，常量，静态变量，即时变异器编译的代码，低版本用堆中的永久代实现
        1.8将永久代数据分到了堆和元空间中
        元空间：存储类的元信息，放在直接内存
        堆：静态变量和常量池等放入堆中
        此区域异常：OutOfMemoryError

运行时常量池：方法区一部分，存储常量信息，详细参考方法区

java堆：线程共享，存放对象实例

随着JIT编译器的发展与逃逸分析技术成熟，栈上分配、标量替换优化技术，所有对象都分配在堆上也不是很绝对了

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/内存模型.jpg" width="600px">
</div>

## 1.2、判断对象是否存活的算法

引用计数法：无法解决相互引用问题

可达性分析：通过GC Roots判断

    ①：虚拟机栈（栈帧中的本地变量表）中引用的对象
    ②：本地方法栈中JNI（native方法）应用的对象
    ③：方法区中静态类属性，常量引用的对象

## 1.3、引用类型

强引用：只要引用存在，就不会回收

软引用：描述一些还有用但非必须的对象，如果第一次回收还是没有足够内存，那就进行第二次回收

弱引用：非必须对象，生存到下一次GC发生之前

虚引用：将对象设置成虚引用的唯一目的是，能在这个对象被GC时收到一个系统通知


## 1.4、垃圾回收算法

标记-清除

    效率不高，产生大量内存碎片

复制（新生代）
    
    内存等容量划分，一块用完，就将还存活的对象复制到另一块
    效率高，内存利用率仅有一般

标记整理（老年代）

    所有存活对象都向一端移动，清除端边界以外内存
    不能使用复制算法，因为没有"担保"了

## 1.5、新生代老年代内存如何回收

调查表明，98%对象都会被一次回收，所以，新生代不需要按照1:1来划分空间

新生代划分为Eden和两块Survivor，比例为8:1:1，每次只是用一个Survivor和Eden，回收时，将还存活的对象复制到另一个survivor中

两个survivor保证每次都复制到一个空的区域

老年代与新生代的比例为2:1

## 1.6、垃圾回收器有哪些

七大垃圾回收器

①Serial

    新生代，单CPU单线程，STOP THE WORLD

②ParNew

    就是Serial的多线程版本，除了Serial，唯一一个能与CMS配合工作的

③Parallel Scavenge

    新生代，多线程，关注吞吐量（程序运行时间/（程序运行时间+垃圾收集时间））
    自适应调节策略，很多参数无需设置（Eden和survivor比例，晋升老年代大小等）
    会根据系统收集信息，动态调整停顿时间和吞吐量大小

④Serial Old

    Serial的老年代版本，标记整理

⑤Parallel Old

    Parallel Scavenge老年代版本

⑥CMS（Concurrent Mark Sweep）

    老年代，最短回收停顿时间为目标的收集器，并发，"标记清除"
    
    初始标记：标记出GC ROOTS能直接关联到的对象（很快，stop the world）
    并发标记：进行GC ROOTS根搜索算法阶段（慢，与用户线程一起）
    重新标记：修正并发标记期间因用户线程导致标记变动的对象（快，stop the world）
    并发清除：清除（慢，与用户线程一起）
    
    总结：耗时最长的并发标记和清除过程是与用户线程一起工作的，所以可以看出与用户线程并发执行

    缺点：对CPU敏感，（默认启动线程：CPU数量+3/4），CPU很少的情况，性能下降明显
          无法处理浮动垃圾，因为是并发，所以需要预留一部分内存供用户产生浮动垃圾（1.5 68%,1.6 92%）
                            如果预留内存无法满足，就会Concurrent Mode Failure
                            启动Serial Old收集老年代，停顿时间较长
          标记清除产生大量空间碎片，可以通过设置参数，当CMS顶不住，就进行一次内存合并
                                    也可以设置多少次不压缩的Full GC后，进行一次压缩GC

⑦G1（Garbage-First）

    1.7问世，未来可以替换掉1.5的CMS，不需要配合其他收集器管理GC
    
    整体基于标记整理，局部（region）之间是复制
    
    可预测停顿，长度为M毫秒的时间片段内，消耗在GC上运行的时间不得超过N毫秒

    将整个java堆划分成多个大小相等的独立区域（Region），新生代，老年代不再物理隔离
    
    每个region通过自己的Remembered Set实现垃圾回收局部化而不会扩散到全局

    初始标记：GC ROOTS能直接关联的对象，并修改TAMS（很快，stop the world）
    并发标记：可达性分析，找出存活对象（慢，与用户程序并发）
    最终标记：修正并发标记期间因用户线程继续运行导致标记变化的记录
              JVM将变化记录在了Remembered Set Logs里，合并到set中即可（快，stop the world）
    筛选回收：对各个region回收价值和成本排序，根据用户期望的GC停顿时间来制定回收计划

CMS：能否多线程请参考图
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/CMS.png" width="650px">
</div>
G1
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/G1.jpg" width="650px">
</div>

## 1.7、常用的JDK命令行工具

jps
    
    显示系统所有hotspot虚拟机进程

jstat
    
    收集虚拟机各方面的运行数据

jinfo

    显示虚拟机配置信息
    
jmap

    生成虚拟机的内存转储快照（heapdump文件）
    
jhat

    分析heapmap文件，会建立一个http/html服务器

jstack

    显示虚拟机线程快照

JConsole、VisualVM

## 1.8、类加载器分类

启动类加载器
    
    将java_home/lib下的类库加载到内存

扩展类加载器

    将java_home/lib/ext下或被java.ext.dirs系统变量指定的路径的类库加载到内存

应用程序加载器

    用户类路径上所指定的类库

用户自定义加载器

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/类加载器.jpg" width="650px">
</div>



# 二、多线程

# 三、基础知识

## 3.1、JVM&JRE&JDK

JVM：Java Virtual Machine，java虚拟机，加载并运行java程序

JRE：Java Runtime Environmental，java运行时环境，包括JVM和java的常用类库，是java程序运行的最小环境

JDK：Java Development ToolKit，java开发工具包，除了JRE和JVM外，还包含java，javac等一些工具










