package spark.spark3step

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * FileName: StructuredStreamingWordCount
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-6-1 下午3:34
  * Description:
  * Structured Streaming例子
  *
  */
object StructuredStreamingWordCount {

  def main(args: Array[String]): Unit = {

    //设置配置
    val conf = new SparkConf()
      .setAppName("StructuredStreamingWordCount")
      .setMaster("local[2]")

    /**
      * 第一步：创建程序入口SparkSession ,并引入spark.implicits._来允许 Scalaobject 隐式转换为DataFrame
      */

    val spark = SparkSession.builder().config(conf).getOrCreate()

    import spark.implicits._

    /**
      * 第二步：创建流。配置从 socket 读取流数据,地址和端口为 localhost: 9999。
      */
    val lines = spark.readStream.format("socket")
      .option("host","localhost")
      .option("port","9999")
      .load()
    /**
      * 第三步:进行单词统计。这里 lines 是 DataFrame ,使用 as[String]给它定义类型转换为
      * Dataset 。 之后在 Dataset 里进行单词统计。
      */
    val words = lines.as[String].flatMap(_.split(" "))

    val wordCount = words.groupBy("value").count()
    /**
      * 第四步:创建查询句柄,定义打印结果方式并启动程序 。 这里使用 write Stream 方法 , 输
      * 出模式为全部输出到控制台。
      */
    val query = wordCount.writeStream.outputMode("complete").format("console").start()
    query.awaitTermination()

    /**
      * 第五步：接下来运行该程序,在 Linux 命令窗口运行 nc-lk 9999 开启 9999 端口 。
      */
  }

}
