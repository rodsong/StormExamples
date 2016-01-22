package com.demo2;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.demo2.bolts.ReportBolt;
import com.demo2.bolts.SplitSentenceBolt;
import com.demo2.bolts.WordCountBolt;
import com.demo2.spouts.SentenceSpout;

/**
 * 增加Topology 计算能力
 */
public class WordCountTopology2 {
    private static final String SENTENCE_SPOUT_ID = "sentence-spout-id";
    private static final String SPLIT_BOLT__ID = "split-bolt-id";
    private static final String COUNT_BOLT__ID = "count-bolt-id";
    private static final String REPORT_BOLT__ID = "report-bolt-id";
    private static final String TOPOLOGY_NAME = "word-count-topology";

    public static void main(String[] args) throws InterruptedException {
        //定义 Topology
        TopologyBuilder builder = new TopologyBuilder();

        //注册一个spout
        builder.setSpout(SENTENCE_SPOUT_ID, new SentenceSpout(),2);

        //注册一个bolt，这个bolt订阅 SENTENCE_SPOUT_ID 发出的数据流
        //shuffleGrouping 方法告诉storm要将spout(SENTENCE_SPOUT_ID)发射的tuple随机均匀分发给SplitSentenceBolt实例
        //启动2个executor，每个运行2个task任务（4个task）
        builder.setBolt(SPLIT_BOLT__ID, new SplitSentenceBolt(),2).setNumTasks(4).shuffleGrouping(SENTENCE_SPOUT_ID);

        //fieldsGrouping方法确保相同的word值的tuple被路由到相同的 WordCountBolt实例中
        //启动2个executor(thread)执行WordCountBolt实例
        builder.setBolt(COUNT_BOLT__ID, new WordCountBolt(),10).fieldsGrouping(SPLIT_BOLT__ID, new Fields("word"));
        //错误分组方式？why？
        //builder.setBolt(COUNT_BOLT__ID, new WordCountBolt(),10).shuffleGrouping(SPLIT_BOLT__ID);

        //globalGrouping方法表示我们希望所有的 WordCountBolt 实例发射的 tuple 流都路由到 ReportBolt实例
        //为什么不增加report的并发数？why？
        builder.setBolt(REPORT_BOLT__ID, new ReportBolt()).globalGrouping(COUNT_BOLT__ID);

        Config config = new Config();

        config.setNumWorkers(2);//增加work(jvm)
        //本地模式
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());

        //运行5秒后停止并关闭该topology
        try {
            Thread.sleep(1000 * 5);
            cluster.killTopology(TOPOLOGY_NAME);

            Thread.sleep(1000 * 5);
            cluster.shutdown();
        } catch (InterruptedException e) {
        }

        System.exit(0);
    }
}