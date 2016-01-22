package com.demo1;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.demo1.bolt.SequenceBolt;
import com.demo1.spout.RandomSpout;

/**
 * 描述
 *
 * @author songyanfei
 * @version 1.0
 * @date 2016年01月18日 added
 */
public class FirstTopology {
    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout_id", new RandomSpout());
        builder.setBolt("bolt_id", new SequenceBolt()).shuffleGrouping("spout_id");
        Config conf = new Config();
        conf.setDebug(false);
        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
        } else {

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("firstTopo", conf, builder.createTopology());
            Utils.sleep(100000);
            cluster.killTopology("firstTopo");
            cluster.shutdown();
        }
    }
}
