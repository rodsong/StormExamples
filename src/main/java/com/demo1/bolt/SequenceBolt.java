package com.demo1.bolt;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

/**
 * 描述
 *
 * @author songyanfei
 * @version 1.0
 * @date 2016年01月18日 added
 */
public class SequenceBolt extends BaseBasicBolt {
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String word = (String) input.getValue(0);
        String out = "I'm " + word +  "!";
        System.out.println("out=" + out);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
