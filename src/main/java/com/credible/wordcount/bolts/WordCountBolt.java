package com.credible.wordcount.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

public class WordCountBolt extends BaseRichBolt{
	Integer id;
	String name;
	Map<String, Long> counters;
	private OutputCollector collector;

	/**
	 * 准备该bolt所用资源
 	 * @param stormConf
	 * @param context
	 * @param collector
	 */
	@Override
	public void prepare(Map stormConf, TopologyContext context,	OutputCollector collector) {
		this.counters = new HashMap<>();
		this.collector = collector;
		this.name = context.getThisComponentId();
		this.id = context.getThisTaskId();

	}


	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	    declarer.declare(new Fields("word","count"));
	}

	@Override
	public void execute(Tuple input) {
		String str = input.getStringByField("word");
	    Long count = this.counters.get(str);
		if(count == null){
			count=0L;
		};
		count++;
		this.counters.put(str, count);
		this.collector.emit(new Values(str, count));
	}
	/**
	 * Topology执行完毕的清理工作，比如关闭连接、释放资源等操作都会写在这里
	 * 因为这只是个Demo，我们用它来打印我们的计数器
	 * */
	@Override
	public void cleanup() {
		System.out.println("-- Word Counter [" + name + "-" + id + "] --");
		for (Map.Entry<String, Long> entry : counters.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		counters.clear();
	}


}