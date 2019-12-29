* [一、虚拟机](一虚拟机)
    * [1、内存模型](#1内存模型)
    * [2、判断对象是否存活的算法](#2判断对象是否存活的算法)
    * [3、引用类型](#3引用类型)
    * [4、垃圾回收算法](#4垃圾回收算法)
    * [5、新生代老年代内存如何回收](#5新生代老年代内存如何回收)
    * [6、垃圾回收器有哪些](#6垃圾回收器有哪些)
    * [7、常用的JDK命令行工具](#7常用的JDK命令行工具)
    * [8、类加载器分类](#8类加载器分类)
    * [9、类加载过程](#9类加载过程)
    * [10、能否在加载类的时候修改字节码](#10能否在加载类的时候修改字节码)
* [二、多线程](#二多线程)
    * [1、ThreadLocal](#1ThreadLocal)
    * [2、线程状态](#2线程状态)
    
    * [4、线程池默认拒绝策略](#4线程池默认拒绝策略)
    * [5、为什么不推荐使用Executors](#5为什么不推荐使用Executors)
* [三、基础知识](#三基础知识)
    * [1、JVM-JRE-JDK](#1JVM-JRE-JDK)
    * [2、接口与抽象类](#2接口与抽象类)
    * [3、多态](#3多态)
    * [4、内部类](#4内部类)
    * [5、final作用域](#5final作用域)
    * [6、基本数据类型及长度](#6基本数据类型及长度)
    * [7、等等与equals的区别](#7等等与equals的区别)
    * [8、hashCode与equals关系](#8hashCode与equals关系)
    * [9、String与StringBuilder与StringBuffer](#9String与StringBuilder与StringBuffer)
    * [10、String为什么设计成不可变](#10String为什么设计成不可变)
    * [11、String通过什么方式保证不可变](#11String通过什么方式保证不可变)
    * [12、String一定不可变吗](#12String一定不可变吗)
    * [13、finally](#13finally)
    * [14、异常](#14异常)
    * [15、静态变量与非静态变量](#15静态变量与非静态变量)
    * [16、Object九大方法](#16Object九大方法)
    * [17、Java精度问题](#17Java精度问题)
    * [18、Stream](#18Stream)
    * [19、ParallelStream](#19ParallelStream)
    * [20、数据库查询为什么推荐使用Integer](#20数据库查询为什么推荐使用Integer)
    * [21、动态代理](#21动态代理)
    * [22、ASM](#22ASM)    
# 一、虚拟机

## 1、内存模型

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

## 2、判断对象是否存活的算法

引用计数法：无法解决相互引用问题

可达性分析：通过GC Roots判断

    ①：虚拟机栈（栈帧中的本地变量表）中引用的对象
    ②：本地方法栈中JNI（native方法）应用的对象
    ③：方法区中静态类属性，常量引用的对象

## 3、引用类型

强引用：只要引用存在，就不会回收

软引用：描述一些还有用但非必须的对象，如果第一次回收还是没有足够内存，那就进行第二次回收

弱引用：非必须对象，生存到下一次GC发生之前

虚引用：将对象设置成虚引用的唯一目的是，能在这个对象被GC时收到一个系统通知


## 4、垃圾回收算法

标记-清除

    效率不高，产生大量内存碎片

复制（新生代）
    
    内存等容量划分，一块用完，就将还存活的对象复制到另一块
    效率高，内存利用率仅有一般

标记整理（老年代）

    所有存活对象都向一端移动，清除端边界以外内存
    不能使用复制算法，因为没有"担保"了

## 5、新生代老年代内存如何回收

调查表明，98%对象都会被一次回收，所以，新生代不需要按照1:1来划分空间

新生代划分为Eden和两块Survivor，比例为8:1:1，每次只是用一个Survivor和Eden，回收时，将还存活的对象复制到另一个survivor中

两个survivor保证每次都复制到一个空的区域

老年代与新生代的比例为2:1

## 6、垃圾回收器有哪些

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
    
    每个region通过自己的Remembered Set实现垃圾回收局部化而不会扩散到全局，RS也记录region之间的引用

    初始标记：GC ROOTS能直接关联的对象，并修改TAMS（很快，stop the world）
    并发标记：可达性分析，找出存活对象（慢，与用户程序并发）
    最终标记：修正并发标记期间因用户线程继续运行导致标记变化的记录
              JVM将变化记录在了Remembered Set Logs里，合并到set中即可（快，stop the world）
    筛选回收：对各个region回收价值和成本排序，根据用户期望的GC停顿时间来制定回收计划

    -XX:G1HeapRegionSize=n可指定分区大小（1到32MB，必须是2的幂），默认将整个堆划分为2048个分区
    每个region又被分成若干大小为512Byte的Card，每次对内存的回收就是对指定分区的卡片进行处理，对象分配以Card为单位
    
    分代：逻辑上划分为年轻代和老年代，但年轻代不是固定不变的，当年轻代满了，JVM会分配新的空闲分区加入到年轻代
    年轻代会在-XX:G1NewSizePercent(默认为堆5%)与最大空间-XX:G1MaxNewSizePercent(默认60%)之间动态变化
    且由参数目标暂停时间-XX:MaxGCPauseMillis(默认200ms)，需要扩缩容大小以及分区的RSet计算得到
    G1依然可以设置固定年轻代大小（-XX:NewRatio），但同时暂停目标将失去意义
    
    GC状态流转顺序：Minor GC -> Minor GC + Concurrent Mark -> Mixed GC
    
    Minor GC:新生代GC，会涉及Remembered Set的处理
    
    Mixed GC:新生代和老年代GC，老年代Region数与总Region数的比值达到该值时，将在Mixed GC中清理老年代
            -XX:G1MixedGCLiveThresholdPercent=85
        设定Mixed GC中清理老年代内存的Region数（比例）
            这是相对与总Region数的占比
            -XX:G1OldCSetRegionThresholdPercent=10
    
    https://blog.csdn.net/coderlius/article/details/79272773

CMS：能否多线程请参考图
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/CMS.png" width="650px">
</div>
G1
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/G1.jpg" width="650px">
</div>

## 7、常用的JDK命令行工具

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

## 8、类加载器分类

启动类加载器
    
    将java_home/lib下的类库加载到内存

扩展类加载器

    将java_home/lib/ext下或被java.ext.dirs系统变量指定的路径的类库加载到内存

应用程序加载器

    用户类路径上所指定的类库

用户自定义加载器

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/类加载器.png" width="650px">
</div>

## 9、类加载过程

#### 加载
    将类加载进内存，生成Class对象

#### 校验
    校验Class文件格式的规范，文件格式，元数据，字节码，符号引用

#### 准备
    在方法区为Class分配内存，并设置static成员变量的初始值为默认值

#### 解析
    将常量池中的符号引用替换为直接引用，主要针对类，接口，方法，成员变量等符号引用

#### 初始化
    在内存中构造一个Class对象表示该类，执行<clinit>()
    <clinit>是对static变量进行赋值操作，虚拟机保证线程安全

#### 使用

#### 卸载

## 10、能否在加载类的时候修改字节码

基于JavaAgent和Java字节码注入技术(ASM)

# 二、多线程

## 1、ThreadLocal
每个线程Thread内部都有一个ThreadLocal.ThreadLocalMap类型的成员threadLocals
    
    Thread类里：ThreadLocal.ThreadLocalMap threadLocals = null;
    每个线程自己维护
这个map的key就是Thread.currentThread，value就是变量副本

## 2、线程状态
### new
初始状态，线程被构建
### runnable
运行状态（对应操作系统的就绪和运行两种状态）
### blocked
阻塞状态
### waiting
等待状态
### time_waiting
超时等待状态，可以指定时间自行返回
### terminated
终止状态，表示已经执行完

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/多线程.png" width="650px">
</div>

## 4、线程池默认拒绝策略

##### AbortPolicy
直接抛异常，默认策略

##### CallerRunsPolicy
用调用者线程来运行

##### DiscardOldPolicy
丢弃队列中最早的一个任务，再尝试添加

##### DiscardPolicy
不处理，丢弃掉

## 5、为什么不推荐使用Executors

##### OutOfMemoryException
newFixedThreadPool和newSingleThreadExecutor底层队列是LinkedBlockingQueue，长度Integer.MAX_VALUE，可以认为无限长

newCachedThreadPool，maximumPoolSize为Integer.MAX_VALUE，可以认为无限大，导致内存溢出
队列用的是SynchronousQueue，默认不存任何元素

newScheduledThreadPoll与newCachedThreadPool一样问题

##### 不能自己实现拒绝策略

##### 创建线程时，不能指定有意义的名字

# 三、基础知识

## 1、JVM-JRE-JDK

JVM：Java Virtual Machine，java虚拟机，加载并运行java程序

JRE：Java Runtime Environmental，java运行时环境，包括JVM和java的常用类库，是java程序运行的最小环境

JDK：Java Development ToolKit，java开发工具包，除了JRE和JVM外，还包含java，javac等一些工具

## 2、接口与抽象类

### 接口
    
常量默认使用public static final修饰，并且初值
所有方法都是抽象的
成员作用域都是public

### 抽象类

子类和父类之间存在逻辑上的层次结构时，推荐使用抽象类。

## 3、多态

### 编译时多态

方法的重载（多个同名方法）

### 运行时多态

方法的覆盖

## 4、内部类

### 静态内部类

不能访问非静态成员

### 成员内部类

不能有静态成员

### 局部内部类

只能访问方法中定义为final类型的局部变量

### 匿名内部类

## 5、final作用域

属性不可变
方法不可覆盖
类不可继承

## 6、基本数据类型及长度

基本数据类型按值传递，封装类型按引用传递
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/基本数据类型.png" width="650px">
</div>

## 7、等等与equals的区别

### ==

基本数据类型按值比较
引用类型按首地址比较

### equals

默认调用==

## 8、hashCode与equals关系

一般复写equals方法也要覆盖hashcode方法，否则会导致基于散列值的集合不可用

equals和hashcode之间的关系

    equals相等，hashcode必相等
    equals不相等，hashcode可能相等，可能不等
    hashcode返回值不相等，equals必不相等
    hashcode返回值相等，equals可能等，可能不等

## 9、String与StringBuilder与StringBuffer

### String

不可变字符序列

### StringBuilder

可变的线程不安全字符序列

### StringBuffer

可变的线程安全字符序列

## 10、String为什么设计成不可变

### 安全
不仅体现应用中，java类装载机制通过传递类名（字符串）加载类，如果可变，一些人通过自定义类装载机制分分钟黑掉应用

### 性能
String Pool，可在初始化时直接计算出hash值

### 线程安全

## 11、String通过什么方式保证不可变

底层通过数组实现，数组的作用域为private final char value[];

## 12、String一定不可变吗

因为String的value是final的，所以不能指向其他数组对象

那么，我们可以更改数组里的值吗？

可以，通过反射，反射可以访问私有成员，并修改数据

    public static void testReflection() throws Exception {
        String s = "Hello World";
        // 获取String类中的value字段
        Field valueFieldOfString = String.class.getDeclaredField("value");
        // 改变value属性的访问权限
        valueFieldOfString.setAccessible(true);
        // 获取s对象上的value属性的值
        char[] value = (char[]) valueFieldOfString.get(s);
        // 改变value所引用的数组中的第5个字符
        value[5] = '_';
        System.out.println("s = " + s);  //Hello_World
    }

## 13、finally

在return之前执行，finally的return会覆盖其他地点的return

两种情况不会执行finally

    发生异常
    System.exit(0)

## 14、异常

### Throwable
所有异常的父类

### Error(错误)
JVM在运行过程中出现严重的错误，错误不可恢复，如OutOfMemoryError,ThreadDead

### Exception(异常)
#### 检测异常：IO异常，SQL异常
#### 运行时异常：空指针，类型转换，数组越界，缓冲区溢出

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/异常.png" width="650px">
</div>

## 15、静态变量与非静态变量

### 静态变量
内存中只有一个，虚拟机在加载类的过程中为静态变量分配内存，位于方法区，所有实例共享

### 非静态变量
每创建一个实例，虚拟机就会为变量分配一次内存，变量位于堆中，生命周期取决于实例生命周期

## 16、Object九大方法

clone
await
notify
notifyAll
getClass
toString
equals
hashCode
finalize

## 17、Java精度问题
### 小数转二进制
    
小数乘以2，去整数部分作为二进制表示的第一位，再用小数部分乘以2……
依次类推，可能造成死循环
    
    下面我们具体计算一下0.6的小数表示过程
    0.6 * 2 = 1.2 ——————- 1 
    0.2 * 2 = 0.4 ——————- 0 
    0.4 * 2 = 0.8 ——————- 0 
    0.8 * 2 = 1.6 ——————- 1 
    0.6 * 2 = 1.2 ——————- 1 
    ……

### 二进制转换为整数
二进制，从左到右，V[i] * 2^(-i)，i为从左到右的index

    我们再拿0.6的二进制表示举例：1001 1001 1001 1001 
    0.6 = 1 * 2^-1 + 0 * 2^-2 + 0 * 2^-3 + 1 * 2^-4 + ……

### 例子

    以下都是false
    System.out.println(0.05+0.01 == 0.06);
    System.out.println(0.060000000000000005 == 0.06);

## 18、Stream

#### 特性
stream不存储数据
stream不改变源数据
stream延迟执行

#### 为什么要有stream

多个遍历集合的操作，每次都要遍历一次，并且保存中间结果

#### 原理

中间操作只是一种标识，只有结束操作才会触发实际计算
无状态的中间操作不受前面元素影响，有状态中间操作必须等所有元素都处理之后才知道最终结果
短路操作指不用处理全部元素就可以返回结果
每个操作是一个stage，每个stage实现Sink接口
每个stage记录前一个stage和本次操作以及回调函数，构成流水线（双向链表）
Sink完美封装了Stream的每一步操作，并给出[处理->转发]模型

    Sink接口
        void begin      开始遍历元素前调用该方法，通知Sink做好准备
        void end        所有元素遍历完之后调用
        boolean cancellationRequested   是否可以结束操作，可以让短路操作尽快结束
        void accept     遍历元素时调用

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/java/stream操作.png" width="650px">
</div>

## 19、ParallelStream

并行流处理，底层使用Fork/Join框架实现

-Djava.util.concurrent.ForkJoinPool.common.parallelism=N 设置worker数量，默认为CPU核心数

缺点：
    
    线程非安全
    整个程序周期，只使用指定的worker数量，如果某个生产者生产了许多重量级任务，那么其他任务将没有工作线程可用
    

## 20、数据库查询为什么推荐使用Integer

int有默认值0，如果没查出来，Integer会是null，而int会是0，这样会导致判断异常

Integer有很多封装的函数可以调用，同时Java集合只支持包装类型而不支持基本数据类型

## 21、动态代理

### JDK动态代理
java内部反射机制实现，生成类比较高效

目标类必须基于统一的接口(底层proxy字节码也会继承Person，并实现方法)

大体流程：为接口创建代理类的字节码文件，使用ClassLoader将字节码文件加载到JVM，创建代理类实例对象
    
    // 统一接口    
    public interface Person{
        public void buy();
    }
    public class zhangze implements Person {
        @Override
        public void buy() {
            sout("张泽");
        }
    }
    
    public ProxySaler implements InvocationHandler {
        public Object person;
        public ProxySaler(Object person) { this.person = person; }
        // 获取被代理接口实例对象
        public<T> T getProxy() {
            return (T) Proxy.newProxyInstance(person.getClass().getClassLoader(), person.getClass().getInterfaces(), this);
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            sout("befor");
            Object result = method.invoke(person, args);
            sout("after");
            return result;
        }
    }
    // 测试类
    public class Client {
        psvm() {
            // 保存生成的代理类的字节码文件
            System.getProperties().put("sum.misc.ProxyGenerator.saveGeneratedFiles", "true");
            // jdk动态代理
            Person person = new ProxySaler(new Zhangze()).getProxy();
            person.buy();
        }
    }
### cglib动态代理
借助ASM来实现，生成类比较低效(通过将asm生成的类进行缓存解决)，生成类之后的相关执行比较高效

Spring AOP就是借助cglib实现的

    // 被代理的方法
    public class PlayGame {
        public void play() { sout("打篮球"); }
    }
    // 代理类
    public class CglibProxy implements MethodInterceptor {
        public Object newInstall(Object object) {
            return Enhancer.create(object.getClass(), this);
        }
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            sout("热身一会");
            methodProxy.invokeSuper(o, objects);
            sout("打完了");
            return null;
        }
    }
    // 测试类
    public class ProxyTest {
        psvm() {
            CglibProxy cglibProxy = new CglibProxy();
            PlayGame playGame = (PlayGame) cglibProxy.newInstall(new PlayGame());
            playGame.play();
        }
    }
    

## 22、ASM

#### 是什么
通用的Java字节码操控和分析框架，性能相比其他框架更佳

可以通过他修改已有的类，也可以直接生成类

一个方法就是一个帧，帧包含本地变量(数组)和操作栈(一组字节码指令序列)

#### 为什么要用ASM
Java是静态语言，在很多情况下我们需要在运行时动态生成或者增强一些类

典型的应用场景就是AOP




