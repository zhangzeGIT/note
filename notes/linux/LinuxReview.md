* [一、统计URL的top5](一统计URL的top5)
* [二、查看CPU核心数](二查看CPU核心数)



# 一、统计URL的top5

awk '{if ($1 in map){
    map[$1] = map[$1]+1;}
    else{map[$1]=1;}}
    END{for(entry in map){print entry" "map[entry]}}' 
    test.txt |sort -r -k 2|head -n 2

awk '{print $1}' test.txt|sort | uniq -c| sort -r -k 1 | head -n 2

# 二、查看CPU核心数

cat /proc/cpuinfo |grep "processor" | wc -l

cupinfo文件有很多关于CPU的信息，比如逻辑CPU，CPU型号，主频，物理CPU个数等