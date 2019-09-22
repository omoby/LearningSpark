package scala.test

/**
  * FileName: NestingIfElse
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-8-1 下午10:02
  * Description:
  *
  */
object NestingIfElse {
  def main(args: Array[String]): Unit = {
    var x  = 20
    var y = 30
    if (x == 20){
      if (y == 30){
        println("x = 20, y = 30")
      }
    }
  }

}
