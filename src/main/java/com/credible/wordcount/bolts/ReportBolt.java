package com.credible.wordcount.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author songyanfei
 * @version 1.0
 * @date 2016年01月19日 added
 */
public class ReportBolt extends BaseRichBolt {

    private HashMap<String, Long> counts = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        counts = new HashMap<>();
    }

    @Override
    public void execute(Tuple input) {
        String word = input.getStringByField("word");
        Long count = input.getLongByField("count");
        this.counts.put(word, count);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public void cleanup() {
        System.out.println("--- FINAL COUNTS ---");
        List<String> keys = new ArrayList<>();
        keys.addAll(this.counts.keySet());
        for(String key :keys){
            System.out.println(key + " : " + this.counts.get(key));
        }
        System.out.println("--- ----- ---");
    }

}
