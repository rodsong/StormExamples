package com.credible.wordcount.spouts;


import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 *  SentenceSpout 类功能很简单，就是向后端发射一个单值tuple组成的数据流
 *  键值 "sentence"
 *  {"sentence":"hello world"}
 */
public class SentenceSpout extends BaseRichSpout {

    final String[] sentences = {"hello world","hello storm"
            ,"我 爱 中 国"
    };

    //SpoutOutputCollector 发射新tuple到它的其中一个输出消息流
    private SpoutOutputCollector collector;
    private int index = 0;
    private ConcurrentHashMap<UUID,Values> pending;


    /**
     * 通过该方法告诉storm会发送那些数据流，每个tuple包含哪些字段
     * @param declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //声明该tuple包含一个字段:sentence {"sentence":"x x x"}
        declarer.declare(new Fields("sentence"));
    }


    /*
    * Spout通过open方法参数里面提供的SpoutOutputCollector来发射新tuple到它的其中一个输出消息流,
    * 发射tuple的时候spout会提供一个message-id, 后面通过这个message-id来追踪这个tuple。
    *
    * 所有的spout组件在初始化的时候调用该方法
    *
    * @param conf，storm配置信息的map
    * @param context，包含了Topology组件的信息
    * @param collector,提供发射tuple的方法
    **/
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        pending = new ConcurrentHashMap<>();
    }

    /**
     * 一个循环中nextTuple, ack, and fail 都会被调用
     *
     * spout组件的核心方法，向输出的collector 发射tuple
     */
    @Override
    public void nextTuple() {
        Values values = new Values(sentences[index]);
        UUID msgId =UUID.randomUUID();
        this.pending.put(msgId,values);

        this.collector.emit(values,msgId);

        index++;
        if(index >= sentences.length){
            index =0;
        }
    }

    @Override
    public void ack(Object msgId){
        this.pending.remove(msgId);

    }

    @Override
    public void fail(Object msgId){
        this.collector.emit(this.pending.get(msgId), msgId);
    }
}