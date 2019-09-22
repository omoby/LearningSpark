package scala.test

/**
  * FileName: ForTest
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-8-1 下午10:21
  * Description:
  *
  */
object ForTest {
  def main(args: Array[String]): Unit = {
    var condition = 0
    for (condition <- 1 to 10){
      println("Value of condition: "+ condition)
    }
  }
}
