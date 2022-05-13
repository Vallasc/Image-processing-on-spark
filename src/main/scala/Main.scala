import java.io.File
import breeze.linalg.DenseVector
import breeze.plot.Figure
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import breeze.linalg.DenseMatrix
import breeze.linalg.CSCMatrix
import scala.collection.GenSeq
import breeze.linalg.Matrix
import org.apache.spark.rdd.RDD

object Main {
    val padding = 20
    val subHeight = 200
    val subWidth = 200

    def main(args: Array[String]): Unit = {
        println("Start")
        
        //scalaImplementation
        //scalaImplementationParallel
        sparkImplementation
        println("End")
    }

    def scalaImplementation = {
        val image = new Image(new File("./data/nike_noisy.png"))
        //val image = new Image(new File("./data/nike.png"))
        val pixelArray = image.getPixelMatrix(true)
        println("Image")
        println(s"- width: ${image.width}")
        println(s"- height: ${image.height}")

        val pixelMatrix = new DenseMatrix[Int](image.width, image.height, pixelArray)
        val out = denConv(pixelMatrix)

        val image2 = new Image(new File(s"./data/OUT.png"))
        image2.setPixelMatrix(out.data, out.rows, out.cols, true)
        image2.saveImage
    }

    /*def scalaImplementationParallel = {
        val image = new Image(new File("./data/nike_noisy.png"))
        //val image = new Image(new File("./data/nike.png"))
        val pixelArray = image.getPixelMatrix(true)
        val pixelMatrix = new DenseMatrix[Int](image.width, image.height, pixelArray)

        val splitted = splitImage(pixelMatrix, subHeight, subWidth, padding)

        val paddedMatrix = splitted._1
        val matrixes = splitted._2

        //val computed = compute(matrixes.par, denoise, padding, paddedMatrix.cols, paddedMatrix.rows)
        val computed = compute(matrixes.par, denConv, padding, paddedMatrix.cols, paddedMatrix.rows)

        val cleaned = computed (padding to padding + pixelMatrix.rows -1, padding to padding + pixelMatrix.cols -1) 
        val out = cleaned toDenseMatrix

        val image2 = new Image(new File(s"./data/OUT.png"))
        image2.setPixelMatrix(out.data, out.rows, out.cols, true)
        image2.saveImage
    }*/

    def sparkImplementation = {
        val conf = new SparkConf().setAppName("GibbsDenoiser")
                                    //.setMaster("spark://localhost:7077")
                                    .setMaster("local[*]")
        val sc = new SparkContext(conf)

        val image = new Image(new File("./data/nike_noisy.png"))
        val pixelArray = image.getPixelMatrix(true)
        val pixelMatrix = new DenseMatrix[Int](image.width, image.height, pixelArray)

        val splitted = splitImage(pixelMatrix, subHeight, subWidth, padding)

        val paddedMatrix = splitted._1
        val matrixes = sc.parallelize(splitted._2)

        val computed = compute(matrixes, denConv, padding, paddedMatrix.cols, paddedMatrix.rows)

        val cleaned = computed (padding to padding + pixelMatrix.rows -1, padding to padding + pixelMatrix.cols -1) 
        val out = cleaned toDenseMatrix

        val image2 = new Image(new File(s"./data/OUT.png"))
        image2.setPixelMatrix(out.data, out.rows, out.cols, true)
        image2.saveImage
    }

    def denConv(matrix: DenseMatrix[Int]) = 
        conv(
            conv(
                denoise(matrix), 
                DenseMatrix((0.1, 0.1, 0.1), (0.1, 0.1, 0.1), (0.1, 0.1, 0.1))),
                DenseMatrix((-1.0, -1.0, -1.0), (-1.0, 8.0, -1.0), (-1.0, -1.0, -1.0)))
        //conv(matrix, DenseMatrix((-1.0, -1.0, -1.0), (-1.0, 8.0, -1.0), (-1.0, -1.0, -1.0)))
                

    def denoise(matrix: DenseMatrix[Int]) =
        new Denoiser( matrix, 150 ).run()

    def conv(matrix: DenseMatrix[Int], convMatrix: DenseMatrix[Double]) =
        new Convolution( matrix,  convMatrix).run()

    def toDenseMatrix(matrix: Matrix[Int]) =
        DenseMatrix.tabulate[Int](matrix.rows, matrix.cols) (matrix (_, _))

    def splitImage(pixelMatrix: DenseMatrix[Int], subMatrixHeight: Int = -1, subMatrixWidth: Int = -1, padding: Int = 1)
                                                        : (DenseMatrix[Int], Seq[(DenseMatrix[Int], Int, Int, Int, Int)]) = {
        val subHeight = if(subMatrixHeight <= 0) pixelMatrix.rows else subMatrixHeight
        val subWidth = if(subMatrixWidth <= 0) pixelMatrix.cols else subMatrixWidth
        assert(padding <= subHeight)
        assert(padding <= subWidth)
        assert(padding >= 0)
        assert(pixelMatrix.rows >= subHeight)
        assert(pixelMatrix.cols >= subWidth)

        var n = pixelMatrix.cols / subWidth // cols divisions
        var m = pixelMatrix.rows / subHeight // rows divisions
        val restWidth = subWidth - (pixelMatrix.cols % subWidth)
        val restHeight = subHeight - (pixelMatrix.rows % subHeight)

        if(restWidth > 0) n += 1
        if(restHeight > 0) m += 1

        val paddedMatrix = DenseMatrix.zeros[Int](pixelMatrix.rows + (padding*2) + restHeight, pixelMatrix.cols + (padding*2) + restWidth)
        // Set padded image
        paddedMatrix(padding to padding + pixelMatrix.rows -1, padding to padding + pixelMatrix.cols -1) := pixelMatrix
        
        println(m)
        println(n)

        (
            paddedMatrix,
            for { 
                p1 <- 0 until n
                p2 <- 0 until m
            } yield {
                val xFrom = p1 * subWidth
                val xTo = xFrom + subWidth -1
                val yFrom = p2 * subMatrixHeight
                val yTo = yFrom + subMatrixHeight -1

                val xFromPadded = xFrom
                val xToPadded = xTo + padding*2
                val yFromPadded = yFrom
                val yToPadded = yTo + padding*2

                val matrix = paddedMatrix(yFromPadded to yToPadded, xFromPadded to xToPadded).copy
                
                (matrix, xFrom, xTo, yFrom, yTo)
            }
        )
    }

    def compute(matrixes : RDD[(DenseMatrix[Int], Int, Int, Int, Int)], transform: (DenseMatrix[Int]) => (DenseMatrix[Int]),
                    padding: Int, paddedImageWidth: Int, paddedImageHeight: Int) = {
        matrixes.map( element => {
                val outMatrix =  CSCMatrix.zeros[Int](paddedImageHeight, paddedImageWidth)
                val transformed = transform (element._1) (padding to -padding -1, padding to -padding -1)

                outMatrix(element._4 + padding to element._5 + padding, element._2 + padding to element._3 + padding) := transformed
                outMatrix
            })
            .reduce( (matrix1, matrix2) => matrix1 + matrix2) 
    }

    def time[R](block: => R): R = {
        val t0 = System.nanoTime()
        val result = block    // call-by-name
        val t1 = System.nanoTime()
        println("Elapsed time: " + (t1 - t0)/1000000 + "ms")
        result
    }
}

// sbt assembly
// spark-submit --deploy-mode client --master spark://localhost:7077 --total-executor-cores 1 --class Main --driver-memory 1G --executor-memory 1G target/scala-2.12/gibbs-image-denoiser-assembly-1.0.jar