package scala.test

/**
  * FileName: DoubleIfTest
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-8-1 下午9:55
  * Description:
  *
  */
object DoubleIfTest {
  def main(args: Array[String]): Unit = {
    var x = 30
    if ( x == 10){
      println("x 的值为 10")
    }else if(x ==20){
      println("x 的值为 20")
    }else if(x == 30){
      println("x 的值为 30")
    }else{
      println("无法判断 x 的值")
    }
  }

}
