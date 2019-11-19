* [一、HTTP，TCP，UDP](#一HTTP,TCP,UDP)
* [二、TCP三次握手](#二HTTP三次握手)
* [三、为什么需要三次握手](#三为什么需要三次握手)
* [四、TCP为什么安全可靠]()
* [五、TCP流量控制](TCP流量控制)
* [六、虚拟机]()


# 一、HTTP，TCP，UDP

TCP/IP是个协议组，可分为三层：网络层，传输层和应用层

网络层：IP,ICMP,ARP,RARP,BOOTP
传输层：TCP,UDP
应用层：FTP,HTTP,TELNET,SMTP,DNS

HTTP是基于TCP协议，是TCP协议族的一种，使用TCP80端口

TCP是面向连接的可靠传输协议，UDP是面向非连接的不可靠传输协议，ping命令就是UDP的一种


# 二、HTTP三次握手
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
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/三次握手.png" width="800px">
</div>


# 三、为什么需要三次握手  

网络的抖动，导致client发送的请求报文延迟，直到某个时间点才到达server
这个请求已经是过期的请求了，如果不是三次握手，server端返回给client端确认，client端不会处理
server端认为已经创建了连接，等待客户端发送数据，导致server端的资源浪费

# 四、TCP为什么安全可靠

超时重传机制
    
    发送方发送的报文含有序列号，每发送一个报文后，就启动一个计时器（RTO）
    
    在计时范围内，如果没有收到响应的ACK，发送方就是重传该报文
    
快速重传机制

    当接收方发送接受的序列号不对的时候（出现了丢包），发送连续三个ACK标至
    
    发送方接收到某个相同序号的三个ACK报文，立马重发该报文，不用等待计时器时间结束

RTO如何计算
    
    由当前网络决定
    
    RTT：一个报文从发送到接收到对应的ACK标志的时间
    
    RTO：发送方尝试发送几个报文，取平均RTT时间来决定

传输过程
    
    每次ACK号 = Seq号 + 传递的字节数 + 1
<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/TCP数据传输.png" width="600px">
</div>
    
# 五、TCP流量控制

    活动传空








