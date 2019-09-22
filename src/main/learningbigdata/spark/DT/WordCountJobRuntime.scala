package spark.DT

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * FileName: WordCountJobRuntime
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-8-1 下午11:32
  * Description:
  *
  */
object WordCountJobRuntime {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ALL)
    val conf = new SparkConf()
      .setAppName("WordCountJobRuntime")
      .setMaster("local[1]")
    val spark =  SparkSession.builder().config(conf).getOrCreate()
    val sc = spark.sparkContext
    val file = sc.textFile("file:///usr/local/spark/README.md")
    val words  = file.flatMap(_.split(" "))
    val wordsPairs = words.map(word=>(word,1))
    val wordCounts = wordsPairs.reduceByKey(_+_)
    wordCounts.saveAsTextFile("file:///home/hadoop/桌面/spark4")
    wordCounts.collect.foreach(println)

  }

}
