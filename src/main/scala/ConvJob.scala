import java.io.File
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.distributed.MatrixEntry
import org.apache.spark.mllib.linalg.distributed.CoordinateMatrix

object ConvJob extends Job {

    val inputImage = new Image(new File("./data/testo_noisy.png"))
    val outputImage = new Image(new File("./data/OUT.png"))

    val padding = 10
    val subHeight = 100
    val subWidth = 100

  override def main(args: Array[String]): Unit = {
        println("Start")
        val t = Utils.time(run)
        println("Time: " + t)
        println("End")
  }

  override def run(): Unit = {
      val conf = new SparkConf().setAppName("GibbsDenoiser")
                              .setMaster("local[*]")
      //conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      //conf.registerKryoClasses(Array(classOf[Tuple2[Tuple2[Int, Int], Matrix]]))

      val sc = new SparkContext(conf)

      val pixelArray = inputImage.getPixelMatrix(true)
      //val pixelMatrix = new BDM[Double](inputImage.width, inputImage.height, pixelArray.map(_.toDouble))


      //val splitted = splitImage(pixelMatrix)
      val entries = sc.parallelize(Seq(
            (0, 0, 3.0), (2, 0, -5.0), (3, 2, 1.0),
            (4, 1, 6.0), (6, 2, 2.0), (8, 1, 4.0))
      ).map{case (i, j, v) => MatrixEntry(i, j, v)}

      val coordinateMatrix = new CoordinateMatrix(entries, 9, 3)
      //coordinateMatrix.
  }

  
}
