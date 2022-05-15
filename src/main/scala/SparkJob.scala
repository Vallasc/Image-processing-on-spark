import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File
import breeze.linalg.{CSCMatrix => BSM, DenseMatrix => BDM, Matrix => BM}
import org.apache.spark.rdd.RDD

import org.apache.spark.mllib.linalg.{Matrix, Matrices}
import org.apache.spark.mllib.linalg.distributed.BlockMatrix

object SparkJob  extends Job {
    val inputImage = new Image(new File("./data/testo_noisy.png"))
    val outputImage = new Image(new File("./data/OUT.png"))

    val padding = 10
    val subHeight = 500
    val subWidth = 500

    def main(args: Array[String]): Unit = {
        println("Start")

        val t = Utils.time(run)
        println("Time: " + t)
        println("End")
    }

    def run(): Unit = {
        val conf = new SparkConf().setAppName("GibbsDenoiser")
                                    //.setMaster("spark://localhost:7077")
                                    //.setMaster("local[*]")

        val sc = new SparkContext(conf)

        val pixelArray = inputImage.getPixelMatrix(true)
        val pixelMatrix = new BDM[Double](inputImage.width, inputImage.height, pixelArray.map(_.toDouble))

        
        val splitted = splitImage(pixelMatrix)

        var n = (pixelMatrix.cols) / subWidth // cols divisions
        var m = (pixelMatrix.rows) / subHeight // rows divisions

        val mat = sc.parallelize(splitted, n*m*2)
        val computed = compute(mat, processPipelne)

        val blockMat = new BlockMatrix(computed, subHeight, subWidth)
        val out = Utils.matrixAsBreeze(blockMat.toLocalMatrix())
        val cleaned = out(0 to pixelMatrix.rows -1, 0 to pixelMatrix.cols -1).copy

        outputImage.setPixelMatrix(cleaned.data.map(_.toInt), cleaned.rows, cleaned.cols, true)
        outputImage.saveImage
    }


    private def splitImage(pixelMatrix: BDM[Double]): Seq[((Int, Int), Matrix)] = {
        val subHeight = if(this.subHeight <= 0) pixelMatrix.rows else this.subHeight
        val subWidth = if(this.subWidth <= 0) pixelMatrix.cols else this.subWidth
        assert(padding <= subHeight)
        assert(padding <= subWidth)
        assert(padding >= 0)
        assert(pixelMatrix.rows >= subHeight)
        assert(pixelMatrix.cols >= subWidth)

        var n = (pixelMatrix.cols) / subWidth // cols divisions
        var m = (pixelMatrix.rows) / subHeight // rows divisions
        var restWidth = (pixelMatrix.cols % subWidth)
        var restHeight = (pixelMatrix.rows % subHeight)

        if(restWidth > 0){
            n += 1
            restWidth = subWidth - restWidth
        } 
        if(restHeight > 0){
            m += 1
            restHeight = subHeight - restHeight
        } 

        val paddedMatrix = BDM.zeros[Double](pixelMatrix.rows + restHeight + padding*2, pixelMatrix.cols + restWidth + padding*2)
        // Set padded image
        paddedMatrix(padding to padding + pixelMatrix.rows -1, padding to padding + pixelMatrix.cols -1) := pixelMatrix
        
        println("x sub-matrix: " + n)
        println("y sun-matrix: " + m)

        println("matrix x size: " + paddedMatrix.rows)
        println("matrix y size: " + paddedMatrix.cols)
        for { 
            p1 <- 0 until n // X
            p2 <- 0 until m // Y
        } yield {
            val xFromPadded = p1 * subWidth
            val xToPadded = xFromPadded + subWidth + padding*2 -1
            val yFromPadded = p2 * subHeight
            val yToPadded = yFromPadded + subHeight + padding*2 -1
            // println("xfrom" + xFromPadded)
            // println("xto" + xToPadded)
            // println("yfrom" + yFromPadded)
            // println("yto" + yToPadded)
            // println()
            val matrix = paddedMatrix(yFromPadded to yToPadded, xFromPadded to xToPadded).copy
            ((p2, p1), Utils.matrixFromBreeze(matrix))
        }
    }

    private def compute(matrixes : RDD[((Int, Int), Matrix)], transform: (BDM[Double]) => (BDM[Double])): RDD[((Int, Int), Matrix)] = {
        matrixes.map ( element => {
            val matrix = Utils.matrixAsBreeze(element._2)
            val out = transform(matrix)
            (element._1, Utils.matrixFromBreeze(out))
        })
    }
}

// spark-submit --class SparkJob jar/gibbs-image-denoiser-assembly-1.0.jar