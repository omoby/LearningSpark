package spark.spark3step

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoders, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

/**
  * FileName: RDD_Movie_Users_Analyzer
  * Author:   hadoop
  * Email:    3165845957@qq.com
  * Date:     19-5-19 下午4:59
  * Description:
  *
  */
object RDD_Movie_Users_Analyzer {

  //创建case User来封装用户数据
  case class User(UserID:String,Gender:String,Age:String,OccupationID:String, Zip_Code:String)

  //创建case Rating来封装用户评分数据
  case  class Rating(UserID:String,MovieID:String,Rating:Double,Timestamp:String)

  def main(args: Array[String]): Unit = {
    val  conf  = new SparkConf().setMaster("local[*]")
      .setAppName("RDD_Movie_Users_Analyzer")

    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._

    val sc = spark.sparkContext
    sc.setLogLevel("WARN")

    val path = "file:///home/hadoop/movierecommend/dataset/"

    //user.dat UserID|age|Gender|Occuption|Zip-code
    val usersRDD = sc.textFile(path + "user.dat")
    //movies.dat MovieId::Title::Genres
    val moviesRDD = sc.textFile(path+"movies.dat")
    //movies.dat UserID::MovieID::Rating::TimeStamp
    val ratingsRDD = sc.textFile(path + "ratings.dat")

    //RDD: MovieID,Title
    val movieInfo = moviesRDD.map(_.split("::")).map(x=>(x(0),x(1))).cache()
    //RDD: UserId,MovieId,Rating
    val ratings = ratingsRDD.map(_.split("::")).map(x=>(x(0),x(1),x(2))).cache()
    //UserID,Gender
    val usersGender = usersRDD.map(_.split("\\|")).map(x=>(x(0),x(2)))

    /**
      * 测试电影评分中评分最高的电影
      */
    // hightestScoreMovies(movieInfo,ratings)

    /**
      * 测试男性和女性中电影评分最高的电影
      */
    //maleAndFemaleHightestScoreMovie(movieInfo,ratings,usersGender,ratingsRDD)

    /**
      * 电影评分按照时间戳和评分进行二次排序
      */
    //sortedByTimestampAndRatings(ratingsRDD)

    /**
      * 使用DataFrame操作电影评分数据
      */
    //dataFrameOps(usersRDD,moviesRDD,ratingsRDD,spark)

    /**
      * 测试使用dataFrame来操作数据
      */
    //dataFrameCreateTempView(usersRDD,moviesRDD,ratingsRDD,spark)

    /**
      * 测试使用dataSet来操作数据
      */
    dataSetOps(usersRDD,ratingsRDD,spark)

    spark.stop()

  }

  /**
    * 统计出所有电影中评分最高的10部电影和评分
    * @param movieInfo RDD[(MovieID,Title)]
    * @param ratings RDD[(UserId,MovieId,Rating)]
    */
  def hightestScoreMovies(movieInfo:RDD[(String,String)],ratings:RDD[(String,String,String)]){
    println("所有电影中平均分最高（口碑最好）的电影")
    //MovieId,(Rating,Num)
    val  moviesAndRatings = ratings.map(x=>(x._2,(x._3.toDouble,1))) //MovieID,(Rating,1)
      .reduceByKey((x,y)=>(x._1+y._1,x._2+y._2))

    //MovieID,AvgRatings
    val avgRatings = moviesAndRatings.map(x=>(x._1,x._2._1.toDouble/x._2._2))

    //MoveID,(AvgRating,Title)
    avgRatings.join(movieInfo).map(item=>(item._2._1,item._2._2))
      .sortByKey(false).take(10)
      .foreach(record=>println(record._2 +" 评分为：" + record._1))
  }

  /**
    * 统计出男生和女生中最喜欢的前10部电影
    * @param movieInfo RDD[(MovieID,Title)]
    * @param ratings RDD[(UserId,MovieId,Rating)]
    * @param usersGender RDD[(UserID,Gender)]
    */
  def maleAndFemaleHightestScoreMovie(movieInfo:RDD[(String,String)],ratings:RDD[(String,String,String)],usersGender:RDD[(String,String)]): Unit ={

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


  }

  /**
    * 对电影评分进行Timestamp和Rating两个维度进行排序
    * @param ratingsRDD  用户电影评分数据 UserID::MovieID::Rating::TimeStamp
    */

  def sortedByTimestampAndRatings(ratingsRDD:RDD[String]): Unit ={
    println("对电影评分数据以Timestamp和Rating两个维度进行二次排序：")
    //排名，评分数据
    val pairWithSortkey = ratingsRDD.map(line=>{
      val splited = line.split("::")
      (new SecondarySortKey(splited(3).toDouble,splited(2).toDouble),line)
    })
    //直接调用 sortByKey ,此时会按照之前实现的 compare 方法排序
    val  sorted = pairWithSortkey.sortByKey(false)

    //取出排名后的评分记录
    val sortedResult = sorted.map(sortedLine =>sortedLine._2)
    sortedResult.take(10).foreach(println)
  }
  /**
    * 通过DataFrame 实现某部电影观看者中男性和女性不同年龄分别有多少人。
    *
    * @param usersRDD 用户信息RDD UserID|age|Gender|Occuption|Zip-code
    * @param moviesRDD 电影信息RDD MovieID::Title::Genres
    * @param ratingsRDD 用户电影评分数据 UserID::MovieID::Rating::TimeStamp
    * @param spark SparkSession
    */
  def dataFrameOps(usersRDD:RDD[String],moviesRDD:RDD[String],ratingsRDD:RDD[String],spark:SparkSession){
    println("功能一：通过DataFrame实现某部电影观看者中男鞋和女性不同年龄段人数统计")
    //首先把 Users 的数据格式化,即在 RDD 的基础上增加数据的元数据信息
    //user.dat UserID|age|Gender|Occuption|Zip-code
    val schemaForUsers = StructType(
      "UserID|Gender|Age|Occuption|Zip-code".split("\\|")
        .map(column => StructField(column,StringType,true))
    )

    //然后把我们的每一条数据变成以 Row 为单位的数据
    val  usersRDDRows = usersRDD
      .map(_.split("\\|"))
      .map(line=>
        Row(line(0).trim,line(2).trim,line(1).trim,line(3).trim,line(4).trim))
    //使用 SparkSession 的 createDataFrame 方法,结合 Row 和 StructType 的元数据信息
    //基于 RDD 创建 DataFrame ,这时 RDD 就有了元数据信息的描述
    val usersDataFrame = spark.createDataFrame(usersRDDRows,schemaForUsers)
    usersDataFrame.show(10)

    //也可以对 StructType 调用 add 方法来对不同的 StructField 赋予不同的类
    val schemaForRatings = StructType("UserID::MovieID".split("::")
      .map(column => StructField(column,StringType,true)))
      .add("Rating",DoubleType,true)
      .add("Timestamp",StringType,true)

    val  ratingsRDDRows = ratingsRDD
      .map(_.split("::"))
      .map(line =>
        Row(line(0).trim,line(1).trim,line(2).trim.toDouble,line(3).trim))
    val ratingsDataFrame = spark.createDataFrame(ratingsRDDRows,schemaForRatings)

    ratingsDataFrame.show(10)
    //接着构边 movies 的 DataFrame
    val schemaForMovies = StructType(
      "MovieID::Title::Genres".split("::")
        .map(column=>StructField(column,StringType,true))
    )

    val moviesRDDRows = moviesRDD
      .map(_.split("::"))
      .map(line=>
        Row(line(0).trim,line(1).trim,line(2).trim))

    val moviesDataFrame = spark.createDataFrame(moviesRDDRows,schemaForMovies)

    moviesDataFrame.show(10)
    // 这里能够直接通过列名MovieID 为 1193 过滤出这部电路, 这些列名都是在上面指定的
    ratingsDataFrame.filter(s"MovieID = 1193")
      //Join 的时候直接指定基于 UserID 进行Join,这相对于原生的 RDD 操作而言更加方便快捷
      .join(usersDataFrame,"UserID")
      //直接通过元数据信息中的Gender和Age进行数据筛选
      .select("Gender","Age")
      //直接通过元数据信息中的 Gender和Age 进行数拙的 groupBy 操作
      .groupBy("Gender","Age")
      //基于 groupBy 分组信息进行 count 统计操作,并显示分组统计后的10条信息
      .count()
      .show(10)
  }


  /**
    * 使用临时表和全局表实现某一部电影中不同性别年龄分别有多少人
    * @param usersRDD 用户信息RDD UserID|age|Gender|Occuption|Zip-code
    * @param moviesRDD 电影信息RDD MovieID::Title::Genres
    * @param ratingsRDD 用户电影评分数据 UserID::MovieID::Rating::TimeStamp
    * @param spark SparkSession
    */
  def dataFrameCreateTempView(usersRDD:RDD[String],moviesRDD:RDD[String],ratingsRDD:RDD[String],spark:SparkSession): Unit ={
    println("功能二：用LocalTempView实现某部电影光看着中不同性别不同年龄分别有多少人？")
    //首先把 Users 的数据格式化,即在 RDD 的基础上增加数据的元数据信息
    //user.dat UserID|age|Gender|Occuption|Zip-code
    val schemaForUsers = StructType(
      "UserID|Gender|Age|Occuption|Zip-code".split("\\|")
        .map(column => StructField(column,StringType,true))
    )

    //然后把我们的每一条数据变成以 Row 为单位的数据
    val  usersRDDRows = usersRDD
      .map(_.split("\\|"))
      .map(line=>
        Row(line(0).trim,line(2).trim,line(1).trim,line(3).trim,line(4).trim))
    //使用 SparkSession 的 createDataFrame 方法,结合 Row 和 StructType 的元数据信息
    //基于 RDD 创建 DataFrame ,这时 RDD 就有了元数据信息的描述
    val usersDataFrame = spark.createDataFrame(usersRDDRows,schemaForUsers)
    usersDataFrame.show(10)

    //也可以对 StructType 调用 add 方法来对不同的 StructField 赋予不同的类
    val schemaForRatings = StructType("UserID::MovieID".split("::")
      .map(column => StructField(column,StringType,true)))
      .add("Rating",DoubleType,true)
      .add("Timestamp",StringType,true)

    val  ratingsRDDRows = ratingsRDD
      .map(_.split("::"))
      .map(line =>
        Row(line(0).trim,line(1).trim,line(2).trim.toDouble,line(3).trim))
    val ratingsDataFrame = spark.createDataFrame(ratingsRDDRows,schemaForRatings)

    ratingsDataFrame.show(10)
    //接着构边 movies 的 DataFrame
    val schemaForMovies = StructType(
      "MovieID::Title::Genres".split("::")
        .map(column=>StructField(column,StringType,true))
    )

    val moviesRDDRows = moviesRDD
      .map(_.split("::"))
      .map(line=>
        Row(line(0).trim,line(1).trim,line(2).trim))

    val moviesDataFrame = spark.createDataFrame(moviesRDDRows,schemaForMovies)

    //既然使用 SQL 语句，那么表肯定是要有的，所以需要先把 DataFrame 注册为临时表
    ratingsDataFrame.createTempView("ratings")
    usersDataFrame.createTempView("users")

    //然后写SQL语句，直接接使用 SparkSession的sql方法执行 SQL 语句即可

    val  sql_local = "SELECT Gender, Age, count(*) from users u join ratings as r " +
      "on u.UserID = r.UserID WHERE MovieID = 1193 GROUP BY Gender, Age"
    println("注册为临时表，查询结果前10条：")
    spark.sql(sql_local).show(10)

    //创建一个Application级别的临时表
    ratingsDataFrame.createGlobalTempView("ratings")
    usersDataFrame.createGlobalTempView("users")

    val sql_global = "SELECT Gender, Age, count(*) from global_temp.users u join "+
    "global_temp.ratings as r on u.UserID = r.UserID WHERE MovieID = 1193 GROUP BY Gender, Age"

    println("注册为全局表，查询结果前10条：")
    spark.sql(sql_global).show(10)

    //引入一个隐式转换实现复杂的功能
    println("使用一个隐式转换实现按照评分平均数据排序")
    import spark.sqlContext.implicits._
    ratingsDataFrame.select("MovieID","Rating")
      .groupBy("MovieID").avg("Rating")
      //接着我们可以使用“$”符号把引号里的字符串转换成列来实现相对复杂的功能，例如，下面
      //我们把 avg(Rating ）作为排序的字段降序排列
      .orderBy($"avg(Rating)".desc)
      .show(10)

    //DataFrame和RDD混编
    println("使用DataFrame和RDD混编实现按照评分平均数据排序")
    ratingsDataFrame.select("MovieID","Rating")
      .groupBy("MovieID").avg("Rating")
      //这电直接使用 DataFrame的rdd方法转到RDD里操作
      .rdd.map(row=>(row(1),(row(0),row(1))))
      .sortBy(_._1.toString.toDouble,false)
      .map(tuple=>tuple._2)
      .collect.take(10).foreach(println)
      }

  /**
    * 通过DataSet实战电影点评系统案例
    * @param usersRDD 用户信息RDD UserID|age|Gender|Occuption|Zip-code
    * @param ratingsRDD 用户电影评分数据 UserID::MovieID::Rating::TimeStamp
    * @param spark SparkSession
    */
  def dataSetOps(usersRDD:RDD[String],ratingsRDD:RDD[String],spark:SparkSession): Unit ={
    //通过DataSet实战电影点评系统案例
    import spark.implicits._

    //将用户数据封装到User class中
    val usersForDSRDD = usersRDD.map(_.split("\\|")).
      map(line=>User(line(0).trim,line(2).trim,line(1).trim,line(3).trim,line(4).trim))

    //最后创建DateSet
    val  userDataSet = spark.createDataset[User](usersForDSRDD)
    userDataSet.show(10)
    //将评分数据封装到Rating class中
    val ratingsForDSRDD = ratingsRDD.map(_.split("::"))
    .map(line=>Rating(line(0).trim,line(1).trim,line(2).trim.toDouble,line(3).trim))

    val ratingsDataSet = spark.createDataset[Rating](ratingsForDSRDD)
    //下面的实现代码和使用 DataFrame方法几乎完全一样（把 DataFrame换成DataSet即可)
    ratingsDataSet.filter(s" MovieID = 1193").join(userDataSet,"UserID")
      .select("Gender","Age")
      .groupBy("Gender","Age")
      .count()
      .orderBy($"Gender".desc,$"Age")
      .show(10)
  }
}
