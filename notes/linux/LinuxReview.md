* [一、统计URL的top5](一统计URL的top5)



# 一、统计URL的top5

awk '{if ($1 in map){
    map[$1] = map[$1]+1;}
    else{map[$1]=1;}}
    END{for(entry in map){print entry" "map[entry]}}' 
    test.txt |sort -r -k 2|head -n 2

awk '{print $1}' test.txt|sort | uniq -c| sort -r -k 1 | head -n 2


