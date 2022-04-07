import java.io.File
import breeze.linalg.DenseVector
import breeze.plot.Figure
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import breeze.linalg.DenseMatrix

object Main {

    def main(args: Array[String]) = {
        println("Start")
        scalaImplementationParallel
        //scalaImplementation
        println("End")
    }

    def sparkImplementation = {
        val conf = new SparkConf().setAppName("GibbsDenoiser").setMaster("spark://localhost:7077")
        val sc = new SparkContext(conf)

        val image = new Image(new File("./data/nike_noisy.png"))
        val pixelMatrix = new DenseMatrix[Int](image.width, image.height, image.getPixelMatrix(true) )
        val N = 2
        val M = 2
        val subWidth : Int = Math.ceil(image.width / N).toInt
        val subHeight : Int = Math.ceil(image.height / M).toInt
        /*sc.parallelize(range((0, N), (0, M)))
            .map( index => pixelMatrix( N * index._1 until index._1, index._2) )*/
        val rdd = sc.parallelize( 
            for { 
                p1 <- 0 until image.width by subWidth
                p2 <- 0 until image.height by subHeight
            } yield (p1, p2))

        val subMatrixes = rdd.map( index => 
                                    pixelMatrix( index._1 until index._1 + subWidth, 
                                                index._2 until index._2 + subHeight) )
        val results = subMatrixes.map( matrix => (new Denoiser( matrix ).run()))
    }

    def range(r: (Int, Int), s: (Int, Int)) = 
        for { p1 <- r._1 until s._1
                p2 <- r._2 until s._2 } yield (p1, p2)

    def scalaImplementation = {
        val image = new Image(new File("./data/nike_noisy.png"))
        val pixelArray = image.getPixelMatrix(true)
        println("Image")
        println(s"- width: ${image.width}")
        println(s"- height: ${image.height}")
        //val image = new Image(new File("./data/testo.png"))
        val denoiser = new Denoiser(pixelArray, image.width, image.height)
        val result = denoiser.run()
        val image2 = new Image(new File("./data/nike_recovered.png"))
        image2.setPixelMatrix(result.data, result.rows, result.cols, true)
        image2.saveImage
    }

    def scalaImplementationParallel = {
        val image = new Image(new File("./data/nike_noisy.png"))
        val pixelArray = image.getPixelMatrix(true)
        val pixelMatrix = new DenseMatrix[Int](image.width, image.height, pixelArray)

        // val im = new Image(new File(s"./data/par/im.png"))
        // im.setPixelMatrix(pixelMatrix.data, pixelMatrix.rows, pixelMatrix.cols, true)
        // im.saveImage

        val N = 4
        val M = 4
        val restWidth = (pixelMatrix.cols % N)
        val restHeight = (pixelMatrix.rows % M)
        val subWidth : Int = (pixelMatrix.cols / N).toInt
        val subHeight : Int = (pixelMatrix.rows / M).toInt

        println(s"width ${pixelMatrix.cols}")
        println(s"height ${pixelMatrix.rows}")
        println(s"restWidth $restWidth")
        println(s"restHeight $restHeight")
        println(s"subWidth $subWidth")
        println(s"subHeight $subHeight")
        println 

        val rdd = 
                (for { 
                    p1 <- 0 until N
                    p2 <- 0 until M
                } yield (p1, p2)).par

        rdd.map( index => {
                val xFrom = index._1 * subWidth
                val xTo = if (index._1 != N-1) xFrom + subWidth -1 
                            else xFrom + subWidth - 1 + restWidth
                val yFrom = index._2 * subHeight
                val yTo = if (index._1 != M-1) yFrom + subHeight -1
                            else yFrom + subHeight -1 + restHeight
                println(xFrom to xTo)
                println(yFrom to yTo)
                println
                val matrix = pixelMatrix( yFrom to yTo, xFrom to xTo).copy
                (index, new Denoiser( matrix ).run())
                //(index, matrix)
            })
            .foreach(matrix => {
                val image2 = new Image(new File(s"./data/par/${matrix._1}.png"))
                image2.setPixelMatrix(matrix._2.data, matrix._2.rows, matrix._2.cols, true)
                image2.saveImage
            })
    }

}
