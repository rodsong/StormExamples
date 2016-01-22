===============================
Storm是一个实时的可靠地分布式流计算框架。
     具体就不多说了，举个例子，它的一个典型的大数据实时计算应用场景：
     从Kafka消息队列读取消息（可以是logs,clicks,sensor data）、通过Storm对消息进行计算聚合等预处理、
     把处理结果持久化到NoSQL数据库或者HDFS做进一步深入分析
	 
==============================

1. Storm topology 组成

   stream 数据流
   spout  数据流的生成着
   bolt   运算着


2. Storm 核心数据结构是tuple

   tuple是包含一个或者多个键值对的集合，Stream是有无限个tuple序列组成。

3. spout
   是storm数据的主要数据入口，连接数据源.

4. bolt
   storm 中的运算或函数，将一个或者多个数据流作为输入，对数据实施运算后，选择性的输出一个或者多个数据流。
   bolt可以订阅多个由spout或者其他bolt 发射（emit）的数据流，这样就建立了复杂的数据转换网络。

5. 本地模式
   Storm的本地模式就是在一个jvm中模拟一个storm集群，方便开发调试。

6. 集群模式

   集群中的topology包含4个组成部分
   a. Nodes   指配置在storm集群中的服务器，每个node会执行topology的部分运算
   b. Workers 指一个node上运行运行几个jvm，每个jvm成为一个worker
   c. Executes work(jvm) 中运行的thread
   d. Task   spout or bolt的实例

   所以集群环境中我们设置worker，execute， task的值来确定有多少集群进行运算。
   注意，local模式中增加worker并不能提高速度，因为本地模式是同一个jvm

7. Tuple数据流分组
   storm有七种数据流分组方式
   1). Shuffle grouping 随机分组，随机分发tuple给bolt的各个task，每个bolt接收相同个数的tuple
   2). Fields grouping 按字段分组，按照字段值进行分组，所有相同值路由到同一个bolt
   3). All grouping 全复制分组，所有的tuple复制路由给所有订阅的bolt
   4). Global grouping 全局分组，将所有的tuple路由到同一个task上，
       全局分组设置bolt的task是没有意义的，因为storm会选择一个最小id的task，所有的tuple都转发的相同的jvm中，可能会出现瓶颈，
       所以使用时要小心性能问题。
   5). None grouping, 功能上跟随机分组一致，为将来版本预留功能
   6). Direct grouping, 指向型分组，数据源调用emitDirect()方法来判断一个tuple应该由哪个storm组件接收
   7). Local or shuffle grouping,本地或随机分组，如果同一个worker(jvm)中有订阅的bolt task，否则采用随机分组方式

8. 容错性
   storm的消息处理机制

   spout的可靠机制
   ack()
   fail()

   bolt的可靠性机制
   tuple的锚钉机制
   collector.emit(tuple,new Values("",""))
   ack(tuple)
   fail(tuple)

   例子 com.credible.wordcount.WordCountTopology3.java


9. Storm集群

   storm集群由一个主节点(nimbus) 和多个工作节点(supervisor 监察着)组成,并且还需要有一个zookeeper实例(一个或多个节点)

   伪集群部署（ubuntu）
   1，首先安装&启动zookeeper
      sudo apt-get --yes install zookeeper zookeeperd

   2, 修改YAML默认配置
       ~/.storm/storm.yaml
       nimbus.host: "localhost"

   3, 启动守护进程
       storm nimbus &
       storm supervisor &
       storm ui &
   http://localhost:8080  查看集群状态


10. 管理命令

    $>storm 用来管理和发布集群中的topology

    jar
    kill
    deactivate
    activate
    rebalance

11. 提交集群作业
    a.修改 WordCountTopologyMain.java集群提交方式

    StormSubmitter.submitTopology(args[0], config, builder.createTopology());

    b.修改pom.xml, storm dependencies jar's scope is provided, 避免jar包冲突
    <dependencies>
            <dependency>
                <groupId>org.apache.storm</groupId>
                <artifactId>storm-core</artifactId>
                <version>0.10.0</version>
                <scope>provided</scope>
            </dependency>
    </dependencies>

    c. mvn clean install

    d. 上传编译好的jar 到集群的某一个目录，例如 examples/storm-test-0.0.1-SNAPSHOT.jar
         upload target storm-test-0.0.1-SNAPSHOT.jar to storm cluster

    e. 提交作业 执行main 名称及参数
        storm jar examples/storm-test-0.0.1-SNAPSHOT.jar com.demo2.WordCountTopologyMain  first-wordcount-test

    f. 查看作业运行状态
        http://10.128.2.150:8080/
        storm list 查看作业

    g. 停止作业

        storm kill first-wordcount-test

12. 调优
    参考 core jar 中的 defaults.yaml 调整参数。例如

    nimbus.host nimbus服务器地址

    supervisor.slots.ports
       supervisor上能够运行workers的端口列表.
       每个worker占用一个端口,且每个端口只运行一个worker.通过这项配置可以调整每台机器上运行的worker数.(调整slot数/每机)

    如果一个topology 所需要的worker 大于集群中worker总数，那么按总worker数量执行。每个supervisor 默认可以启动4个worker(jvm)

13. Trident (storm 事务处理)
    Trident是对Storm的更高一层的抽象,除了提供一套简单易用的流数据处理API之外，
    它以batch(一组tuples)为单位进行处理，这样一来，可以使得一些处理更简单和高效。
    我们知道把Bolt的运行状态仅仅保存在内存中是不可靠的，如果一个node挂掉，
    那么这个node上的任务就会被重新分配，但是之前的状态是无法恢复的。
    因此，比较聪明的方式就是把storm的计算状态信息持久化到database中，
    基于这一点，trident就变得尤为重要。
    因为在处理大数据时，我们在与database打交道时通常会采用批处理的方式来避免给它带来压力，
    而trident恰恰是以batch groups的形式处理数据，并提供了一些聚合功能的API。

    两种不同的运算 filter & function
    三种聚合器
    CombinerAggregator, 合并tuple值
    ReducerAggregator,
    Aggregator, 通用聚合器


14. Trident主要有5类操作：

    1、作用在本地的操作，不产生网络传输。
    2、对数据流的重分布，不改变流的内容，但是产生网络传输。
    3、聚合操作，有可能产生网络传输。
    4、作用在分组流（grouped streams）上的操作。
    5、Merge和join


    首先说几个名词：
    Partition：在Storm中并发的最小执行单元是task；在trident中partition相当于task的角色。
    Grouped streams：对数据流做groupBy操作后，将key相同的流组织在一起，形成若干组流。
    Global aggregation：没有groupBy的聚合，即全局聚合。
    Aggregator：Trident中定义的用于实现聚合方法的接口。









