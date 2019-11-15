# 各种系统专有名词

## kafka

    ISR（In-Sync Replica）：目前可用且消息量与leader相差不多的副本集合
    
    HW（HighWatermark）：只能拉取HW之前的消息，之后的消息对消费者不可见
                        ISR集合全部Follower副本都拉取HW指定的消息进行同步后，leader副本递增HW的值
                        HW之前的消息称为commit
    
    LEO（Log End Offset）：当前副本最后一个消息的offset
                          生成者向leader副本追加消息的时候，leader副本LEO递增
                          follower从leader副本拉取消息并更新都本地的时候，follower副本LEO递增
   