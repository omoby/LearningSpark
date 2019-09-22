package spark.spark.TD

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * FileName: WordCountAgain
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-8-2 下午11:24
  * Description:
  * 再次实现wordcount
  *
  */
object WordCountAgain {
  def main(args: Array[String]): Unit = {
    /**
      * *第 l 步 : 创建 Spark 的配置对象 SparkConf ,设置 Spark 程序运行时的配置信息,
      * *例如,通过 setMaster 设置程序要链接的 Spark 集群的 Master 的 URL , 如果设置
      * *为 local ,则代表 Spark 程序在本地运行,特别适合于机器配置非常差(如果只有只有 1GB
      * *的内存)的初学者
      */
    val conf  = new SparkConf() //创建 SparkConf 对象
      .setAppName("WordCountAgain") //设置应用程序的名称 , 在程序运行的监控界面可以看到名称
      .setMaster("local[2]") //此时程序在本地运行,不需要安装 Spark 集群

    val spark = SparkSession.builder().config(conf).getOrCreate() //sparkSession集成了sparkContext和sqlcontext
    /**
      * 第 2 步 : 创建 SparkContext 对象
      * * SparkContext 是 Spark 程序所有功能的唯一入口,采用 Scala 、 Java 、 Pytho口、
      * * R 等都必须有一个 SparkContext
      * * SparkContext 核心作用:初始化 Spark 应用程序,运行所需要的核心组件,包括
      * * DAGScheduler, TaskScheduler, SchedulerBackend ,同时还会负责 Spark 程
      * *序往 Master 垃册程序等, SparkContext 是整个 Spark 应用程序中至关重要的一个对象
      */
    val sc = spark.sparkContext  //创建sparkContext入口

    /**
      *
      * *第 3 步 z 根据具体的数据来源(如 HDFS 、 HBase 、 Local FS 、 DB 、 S3 等〉通过
      * * SparkContext 来创建 RDD
      * *RDD 的创建有 3 种方式:根据外部的数据来源(如 HDFS )、根据 Scala 集合、由其他
      * *的 RDD 操作
      * 30.
      * *数据会被 RDD :lW 分成为一系列的 Partitions ,分配到每个 Partition 的数据属于
      * *一个 Task 的处理范畴
      */
    //加载spark处理的文件
       val lines = sc.textFile("file:///home/hadoop/Spark/sparkData",1) //读取本地文件并设置为一个 Partition
    /**
      * **
      * *第 4 步:对初始的 ROD 进行 Transformation 级川的处理,且II 通过 map 、 filter ~·f.
      * * 声i阶函数等的编胆, ilUiH 体的数据计算
      * 第 4 . 1 步 :才好也:J .行的字符中拆分成单个单(rIJ
      *
      */
      val words = lines.flatMap(line=>line.split(" "))


      val wordPair = words.map(word=>(word,1))

      val wordCount = wordPair.reduceByKey((a,b)=>(a+b))

 val wordCountOrdered = wordCount.map(pair=>(pair._2,pair._1)).sortByKey(false).map(pair=>(pair._2,pair._1))

 wordCountOrdered.collect().foreach(wordNumberPair=>println(wordNumberPair._1+" : "+wordNumberPair._2))
      sc.stop()
      spark.stop()
      }

 }
