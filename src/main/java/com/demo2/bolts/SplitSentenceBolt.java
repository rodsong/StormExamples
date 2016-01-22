package com.demo2.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  BaseRichBolt 是简单实现，继承这个类只需要实现核心功能。
 */
public class SplitSentenceBolt extends BaseRichBolt {

    private OutputCollector collector;


    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));

    }


    /**
     * 类似 spout 的open方法，作用是初始化bolt所用到的资源，例如数据库连接。
     * @param stormConf
     * @param context
     * @param collector
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    /**
     * 这是bolt中最重要的方法，每当接收到一个tuple时，此方法便被调用
     * 这个方法的作用就是把文本文件中的每一行切分成一个个单词，并把这些单词发射出去（给下一个bolt处理）
     **/
    @Override
    public void execute(Tuple input) {
        //获取tuple中字段 sentence的值
        String sentence = input.getStringByField("sentence");
        String[] words = sentence.split(" ");
        for (String word : words) {
            word = word.trim();
            if (!word.isEmpty()) {
                word = word.toLowerCase();
                // Emit the word
                List a = new ArrayList();
                a.add(input);
                collector.emit(a, new Values(word));
            }
        }
        //确认成功处理一个tuple
        collector.ack(input);
    }

}