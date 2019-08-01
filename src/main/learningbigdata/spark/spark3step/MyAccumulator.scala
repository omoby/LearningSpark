package main.learningbigdata.spark.spark3step

import org.apache.spark.util.AccumulatorV2

import scala.collection.mutable.ArrayBuffer

/**
  * FileName: MyAccumulator
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-6-1 下午2:08
  * Description:
  *首先要继承 AccurnulatorV2 ,并指定输入为 String 类型,输出为 ArrayBuffer[String]
  */
class MyAccumulator extends AccumulatorV2[String,ArrayBuffer[String]] {
  ///设置累加器的结果,类型为 ArrayBuffer[String]
  private var result = ArrayBuffer[String]()
  ///判断累加器当前值是否为“零值”,这里我们指定如果 result 的 size 为 0 ,则累加器的当
  //前值是“零值”
  override def isZero: Boolean = this.result.size == 0

  //copy 方法设置为新建本累加器,并把 result 献给新的累加器
  override def copy(): AccumulatorV2[String, ArrayBuffer[String]] = {
    val  newAccum = new MyAccumulator
    newAccum.result = this.result
    newAccum
  }

  //reset 方法设置为把 result 设置为新的 ArrayBuffer
  override def reset(): Unit = this.result == new ArrayBuffer[String]()

  //add 方法把传进来的字符串添加到 result 内
  override def add(v: String): Unit = this.result += v

  //add 方法把传进来的字符串添加到 result 内
  override def merge(other: AccumulatorV2[String, ArrayBuffer[String]]): Unit = {
    result.++=:(other.value)
  }
  //va lue 方法返回 result
  override def value: ArrayBuffer[String] = this.result
}
