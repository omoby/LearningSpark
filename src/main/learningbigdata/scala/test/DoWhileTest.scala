package scala.test

/**
  * FileName: DoWhileTest
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-8-1 下午10:19
  * Description:
  *
  */
object DoWhileTest {
  def main(args: Array[String]): Unit = {
    var condition = 10
    do{
      println("Value of condition: "+ condition)
      condition += 1
    }while(condition < 20)
  }

}
