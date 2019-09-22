package spark.spark3step

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
  * FileName: TestMyAccumulator
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-6-1 下午2:19
  * Description:
  * 测试自定义字符串合并为数组的累加器
  *
  */
object TestMyAccumulator {
  def main(args: Array[String]): Unit = {
    //设置配置信息
    val conf = new SparkConf().setMaster("local[2]").setAppName("TestMyAccumulator")
    //获取或创建SparkSession
    val spark = SparkSession.builder().config(conf).getOrCreate()

    //获取SparkContext
    val sc = spark.sparkContext

    //创建自定义的累加器
    val myAccum = new MyAccumulator()
    //向 SparkContext 注册累加器
    sc.register(myAccum)
    //把“ a ”“b ”“ c ”“ d”添加进累加器的 result 数组并打印出来
    sc.parallelize(Array("a","b","c","d")).foreach(x=>myAccum.add(x))
    println(myAccum.value.toString())
  }

}
