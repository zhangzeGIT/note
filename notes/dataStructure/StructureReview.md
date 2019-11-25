* [一、布隆过滤器](#一布隆过滤器)

# 布隆过滤器

实际就是一个byte数组，只能大概判断key可能存在

byte数组长这样

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/布隆过滤器1.png" width="650px">
</div>

如果key为baidu，我们经过三次hash，在指定角标中赋值为1

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/布隆过滤器2.png" width="650px">
</div>

在来一个key为tencent

<div align="center">
    <img src="https://github.com/zhangzeGIT/note/blob/master/assets/structure/布隆过滤器3.png" width="650px">
</div>

再来一个，经过同样的hash，判断是否都是一，如果都是，那么就说明可能存在




