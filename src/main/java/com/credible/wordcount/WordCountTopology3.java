package com.credible.wordcount;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import com.credible.wordcount.bolts.ReportBolt;
import com.credible.wordcount.bolts.SplitSentenceBolt;
import com.credible.wordcount.bolts.WordCountBolt;
import com.credible.wordcount.spouts.SentenceSpout;

/**
 * 增加Topology 计算能力
 */
public class WordCountTopology3 {
    private static final String SENTENCE_SPOUT_ID = "sentence-spout-id";
    private static final String SPLIT_BOLT__ID = "split-bolt-id";
    private static final String COUNT_BOLT__ID = "count-bolt-id";
    private static final String REPORT_BOLT__ID = "report-bolt-id";
    private static final String TOPOLOGY_NAME = "word-count-topology";

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        //定义 Topology
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(SENTENCE_SPOUT_ID, new SentenceSpout(),2);
        builder.setBolt(SPLIT_BOLT__ID, new SplitSentenceBolt(),2).setNumTasks(4).shuffleGrouping(SENTENCE_SPOUT_ID);
        builder.setBolt(COUNT_BOLT__ID, new WordCountBolt(),10).fieldsGrouping(SPLIT_BOLT__ID, new Fields("word"));
        //为什么不增加report的并发数？why？
        builder.setBolt(REPORT_BOLT__ID, new ReportBolt()).globalGrouping(COUNT_BOLT__ID);

        Config config = new Config();
        config.setNumWorkers(20);//增加work(jvm)
        //本地模式
        if(args.length ==0){
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());

            //运行5秒后停止并关闭该topology
            Utils.sleep(1000 * 5);

        }else {
            //集群模式
            StormSubmitter.submitTopology(args[0], config, builder.createTopology());
        }

        System.exit(0);
    }
}