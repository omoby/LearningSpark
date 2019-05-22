package main.learningbigdata.spark.spark3step

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * FileName: RDD_Movie_Users_Analyzer
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-5-19 下午4:59
  * Description:
  *
  */
object RDD_Movie_Users_Analyzer {
  def main(args: Array[String]): Unit = {
    val  conf  = new SparkConf().setMaster("local[*]")
      .setAppName("RDD_Movie_Users_Analyzer")

    val spark = SparkSession.builder().config(conf).getOrCreate()

    val sc = spark.sparkContext
    sc.setLogLevel("WARN")

    val path = "file:///home/hadoop/movierecommend/dataset/"

    //user.dat UserID|age|Gender|Occuption|Zip-code
    val usersRDD = sc.textFile(path + "user.dat")
    //movies.dat MovieId::Title::Genres
    val moviesRDD = sc.textFile(path+"movies.dat")
    //movies.dat UserID::MovieID::Rating::TimeStamp
    val ratingsRDD = sc.textFile(path + "ratings.dat")

    println("所有电影中平均分最高（口碑最好）的电影")
    //RDD: MovieID,Title
    val movieInfo = moviesRDD.map(_.split("::")).map(x=>(x(0),x(1))).cache()
    //RDD: UserId,MovieId,Rating
    val ratings = ratingsRDD.map(_.split("::")).map(x=>(x(0),x(1),x(2))).cache()
    //MovieId,(Rating,Num)
    val  moviesAndRatings = ratings.map(x=>(x._2,(x._3.toDouble,1))) //MovieID,(Rating,1)
        .reduceByKey((x,y)=>(x._1+y._1,x._2+y._2))

    //MovieID,AvgRatings
    val avgRatings = moviesAndRatings.map(x=>(x._1,x._2._1.toDouble/x._2._2))

    //MoveID,(AvgRating,Title)
    avgRatings.join(movieInfo).map(item=>(item._2._1,item._2._2))
        .sortByKey(false).take(10)
        .foreach(record=>println(record._2 +" 评分为：" + record._1))

    //UserID,Gender
    val usersGender = usersRDD.map(_.split("\\|")).map(x=>(x(0),x(2)))

    //UserID,((UserID,MovieID,Rating),Gender)
    val genderRatings = ratings.map(x=>(x._1,(x._1,x._2,x._3))) //UserID,(UserID,MovieID,Rating)
      .join(usersGender).cache

    genderRatings.take(10).foreach(println)

    //(UserID,MovieID,Rating),Gender=="M"
    val maleFilteredRatings = genderRatings.filter(x=>x._2._2.equals("M"))
      .map(x=>x._2._1)

    //(UserID,MovieID,Rating),Gender=="M"
    val femaleFiltedRatings = genderRatings.filter(x=>x._2._2.equals("F"))
      .map(x=>x._2._1)

    println("所有电影中男性最喜欢的电影top10:")
    maleFilteredRatings.map(x=>(x._2,(x._3.toDouble,1)))
      .reduceByKey((x,y)=>(x._1+y._1,x._2+y._2))
      .map(x=>(x._1,x._2._1.toDouble/x._2._2))
      .join(movieInfo)
      .map(item=>(item._2._1,item._2._2))
      .sortByKey(false)
      .take(10)
      .foreach(record=>println(record._2+"平分为： "+ record._1))

    println("所有电影中女性最喜欢的电影top10:")
    femaleFiltedRatings.map(x=>(x._2,(x._3.toDouble,1)))
      .reduceByKey((x,y)=>(x._1+y._1,x._2+y._2))
      .map(x=>(x._1,x._2._1.toDouble/x._2._2))
      .join(movieInfo)
      .map(item=>(item._2._1,item._2._2))
      .sortByKey(false)
      .take(10)
      .foreach(record=>println(record._2+"平分为： "+ record._1))

    spark.stop()

  }

}
