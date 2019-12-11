* [一、HTTP-TCP-UDP](#一HTTP-TCP-UDP)
* [二、TCP三次握手](#二TCP三次握手)
* [三、TCP四次挥手](#三TCP四次挥手)
* [四、为什么需要三次握手](#四为什么需要三次握手)
* [五、TCP为什么安全可靠](#五TCP为什么安全可靠)
* [六、TCP流量控制](#六TCP流量控制)
* [七、HTTP和HTTPS的区别](#七HTTP和HTTPS的区别)
* [八、SSL握手](#八SSL握手)
* [九、CA证书](#九CA证书)
* [十、HTTP头](#十HTTP头)
* [十一、HTTP状态码](#十一HTTP状态码)


# 一、HTTP-TCP-UDP

TCP/IP是个协议组，可分为三层：网络层，传输层和应用层

网络层：IP,ICMP,ARP,RARP,BOOTP
传输层：TCP,UDP
应用层：FTP,HTTP,TELNET,SMTP,DNS

HTTP是基于TCP协议，是TCP协议族的一种，使用TCP80端口

TCP是面向连接的可靠传输协议，UDP是面向非连接的不可靠传输协议，ping命令就是UDP的一种


# 二、TCP三次握手
第一次握手：
 
    客户端向服务器发出连接请求报文，这时报文首部中的同部位SYN=1，同时随机生成初始序列号 seq=x，
    此时，TCP客户端进程进入了 SYN-SENT（同步已发送状态）状态。
    TCP规定，SYN报文段（SYN=1的报文段）不能携带数据，但需要消耗掉一个序号。
    这个三次握手中的开始。表示客户端想要和服务端建立连接。
第二次握手：

    服务器收到请求报文后，如果同意，则发出确认报文。报文 ACK=1，SYN=1，确认号是ack=x+1，同时也要为自己随机初始化一个序列号 seq=y
    此时，TCP服务器进程进入了SYN-RCVD（同步收到）状态。
    这个报文也不能携带数据，但是同样要消耗一个序号。
    这个报文带有SYN(建立连接)和ACK(确认)标志，询问客户端是否准备好。
第三次握手：

    客户进程收到确认后，向服务器给出确认。确认报文的ACK=1，ack=y+1，此时，TCP连接建立，客户端进入ESTABLISHED（已建立连接）状态。
    
    TCP规定，ACK报文段可以携带数据，但是如果不携带数据则不消耗序号。这里客户端表示我已经准备好。
    
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/三次握手.png" width="800px">
</div>

# 三、TCP四次挥手

过程
    
    主动方：发送FIN + ACK + SEQ(X)，请求关闭数据传输（这时代表主动方已没有数据）
    
    被动方：发送ACK(X + 1) + SEQ(Z)，返回一个确认
    
    被动方：发送FIN + ACK（X） + SEQ（Y），通知主动方要关闭了
    
    主动方：发送ACK（Y） + SEQ(X)

为什么要等2MSL？

    可能最后的ACK，被动方没有收到，那么被动方就会重复发送FIN，不立即关闭，可以在收到的时候回复
    
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/四次挥手.png" width="800px">
</div>
    
# 四、为什么需要三次握手  

网络的抖动，导致client发送的请求报文延迟，直到某个时间点才到达server
这个请求已经是过期的请求了，如果不是三次握手，server端返回给client端确认，client端不会处理
server端认为已经创建了连接，等待客户端发送数据，导致server端的资源浪费

# 五、TCP为什么安全可靠

超时重传机制
    
    发送方发送的报文含有序列号，每发送一个报文后，就启动一个计时器（RTO）
    
    在计时范围内，如果没有收到响应的ACK，发送方就是重传该报文
    
快速重传机制

    当接收方发送接受的序列号不对的时候（出现了丢包），立即发出重复确认
    
    发送方接收到某个相同序号的三个ACK报文，立马重发该报文，不用等待计时器时间结束
    
    例如：发送M1,M2,M3,M4,M5,M6  M3丢失，接收方收到M4,M5,M6时，会重复发送M2的确认

RTO如何计算
    
    由当前网络决定
    
    RTT：一个报文从发送到接收到对应的ACK标志的时间
    
    RTO：发送方尝试发送几个报文，取平均RTT时间来决定

传输过程
    
    每次ACK号 = Seq号 + 传递的字节数 + 1
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/TCP数据传输.png" width="600px">
</div>
    
# 六、TCP流量控制

解决问题
    
    接收端处理速度不如发送端，消除发送方使接收方缓存溢出的可能
    
滑动窗口协议

    接收方控制流量的一种方式
    
    接收方通过ACK通知发送方窗口大小，越大，网络吞吐量越高
    
    接收方一但发现自己的缓冲区快满了，就将窗口设置成一个更小的值通知发送端
    
    满了，就将窗口设置为0，发送方不在发送数据，但是需要定期发送一个窗口探测数据段，接收方把窗口大小告诉发送方
    
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/滑动窗口.png" width="600px">
</div>
    
拥塞控制
    
    发送方来控制流量的一种方式
    
    拥塞窗口：初始值为1，慢启动阶段，指数增长，非慢启动阶段，每次收到一个ACK，窗口+1
    
    慢启动阈值：ssthresh，初始值为窗口的最大值（大多数TCP的实现是65536）
    
    网络拥塞（拥塞避免）：少了丢包之会重传，大量的丢包，就认为是网络拥塞，慢启动阈值减半，拥塞窗口置为1，重启慢启动
    
    发送方取拥塞窗口和滑动窗口的最小值，作为发送的上限
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/拥塞避免1.png" width="600px">
</div>
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/拥塞避免2.png" width="600px">
</div>

# 七、HTTP和HTTPS的区别

HTTP明文传输（80），HTTPS加密传输（443）

网景设计SSL协议，在HTTP基础上加入SSL（依靠SSL证书来验证服务器），诞生HTTPS

HTTP工作于应用层，HTTPS工作在传输层

HTTP耗时 = TCP握手，HTTPS耗时 = TCP握手 + SSL握手

# 八、SSL握手

客户端发起请求
    
    明文请求，信息包含支持的TSL协议版本、加密套件、压缩算法，随机数（random_c），扩展字段

服务端响应请求

    返回协商的信息结果
    
    包括：协议版本，加密套件，压缩算法，随机数（random_s）
    
    随机数用于后续的密钥协商，服务端配置对应的证书链
    
    通知客户端信息发送结束

客户端校验证书

    校验：证书链的可信性，证书是否吊销（CRL，OCSP），有效期，域名
    
 
客户端密钥交换

    合法性验证通过，客户端计算生成随机数Pre-master，用证书加密，发送给服务端
    
    协商密钥：enc_key = Fuc(random_c, random_s，Pre-master)

    客户端通知服务端后续的通信都采用协商的密钥和加密算法
    
    客户端集合之前所有通信参数的hash值与其他相关信息生成一段数据，采用协商
    的密钥与算法进行加密，然后发送给服务器用于数据与握手验证
    
服务端改变密码规范

    验证数据和密钥：收到加密数据，用私钥解密，基于之前交互的两个明文随机数
                    计算得到协商密钥enc_key=Fuc(randomc_c, random_s, Pre_master)
                    通过计算之前所有接收信息的hash值，解密客户端发送的加密握手消息，验证数据和密钥的正确性
    
    验证通过，服务端告知客户端后续的通信都采用协商的密钥与算法进行加密通信
    
    服务端结合所有当前的通信参数信息，生成一段数据，采用协商密钥加密会话与算法加密并发送到客户端

握手结束

    客户端解密，验证服务器发送的数据和密钥，通过则握手完成

存在问题：

    公钥是公开的，并且可替换，如果有人替换了A的公钥，又用对应的私钥给A发消息，这样就可以冒充

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/SSL握手.png" width="600px">
</div>

# 九、CA证书

可以解决公钥公开的问题

CA是Certificate Authority的缩写，CA证书就是CA发的证书

可申请证书：申请者公钥，申请者的组织信息和个人信息，签发机构信息，有效时间，证书序列号德国明文

签名算法：hash明文信息，然后采用CA的私钥对信息摘要进行加密，密文即签名

过程

    客户端C向服务端S发出请求时，S返回证书文件
    
    C读取证书中的明文信息，hash得到信息摘要，然后用CA公钥解密签名数据，对比证书信息摘要，一致则合法
    
    C会内置信任CA证书的信息（包含公钥）


# 十、HTTP头

### 通用头General

Request URL：请求的URL
Request Method ： 请求的方法，可以是GET、POST 
Status Code：HTTP 状态码，表示请求成功 
Remote Address：远程IP地址 
Referrer Policy：当从一个链接跳到另一个链接，另一个链接的referer就记录了是从哪个链接跳来的。Referrer Policy就是管理这个来源信息的机制。 
                 unsafe-url：无论是同源请求还是非同源请求，都发送完整的 URL（移除参数信息之后）作为引用地址。

### 请求头Request Headers

Accept：浏览器能接收的内容
Accept-Encoding：浏览器支持的压缩编码类型
Accept-Language：浏览器支持的语言
Cookie：HTTP请求发送时，会把保存该请求域名下的所有cookie值一起发送给WEB服务器
Host：指定请求服务器的域名和端口号
Referer：先前网页地址，当前请求网页紧随其后，即来路
User-Agent：发出请求的用户信息
If-Modified-Since
Cache-Control
    
    no-cache：不缓存，每次去服务器去取
    max-age：只接受age值小于max-age值，并且没有过期对象
    max-stale：可以接受过去的对象，但是过期时间必须小于max-stale值
    min-fresh：接受其新鲜生命期大于其当前age跟min-fresh值之和的缓存对象

### 响应头Response Headers

Content-Encoding：内容压缩类型
Content-Length：返回的内容的长度
Content-Type：返回的内容类型
Connection：
Date：请求的日期
Expires：响应过期的时间和日期
Server：服务器
Last-Modified
Cache-Control
    
    public：可以用Cached内容回应任何用户
    private：只能用缓存内容回应先前请求该内容的那个用户
    no-cache：可以缓存，但是只有在跟web服务器验证了其有效后，才能返回给客户端
    max-age：本响应包含的对象的过期时间
    ALL：no-store不允许缓存

### Last-Modified与If-Modified-Since
客户端访问页面，服务端会将页面最后修改时间通过Last-Modified发往客户端

客户端通过If-Modified-Since将先前服务端发过来的最后修改时间戳发送回去

服务端通过这个时间戳判断客户端页面是否最新

    不是最新：返回最新内容
    是最新：返回304告诉客户端其本地cache的页面是最新的

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/http/HttpHeader.jpg" width="600px">
</div>


# 十一、HTTP状态码

1**  信息，服务器收到请求，需要请求者继续执行操作
2**  成功，操作被成功接收并处理
3**  重定向，需要进一步的操作以完成请求
4**  客户端错误，请求包含语法错误或无法完成请求
5**  服务器错误，服务器在处理请求的过程中发生了错误








