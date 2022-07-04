import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File
import breeze.linalg.{CSCMatrix => BSM, DenseMatrix => BDM, Matrix => BM}
import org.apache.spark.rdd.RDD

import org.apache.spark.mllib.linalg.{Matrix, Matrices}
import org.apache.spark.mllib.linalg.distributed.BlockMatrix
import org.apache.spark.ml.image.ImageSchema._
import java.io.PrintWriter
import _root_.Utils.FileUtils
import java.nio.charset.Charset
import java.io.InputStream
import java.io.OutputStream
import Pipelines.Pipeline
import scala.collection.parallel.immutable.ParSeq
import org.apache.spark.HashPartitioner
import org.apache.spark.storage.StorageLevel


class SparkJob(val padding: Int = 3,
                val subHeight: Int = 100,
                val subWidth: Int = 100,
                val denoiserRuns: Int = 80,
                val debug: Int = 1) extends Serializable {

    /**
     * Runs the Job on the spark cluster given a Pipeline
     */
    def run(inputMatrix: BDM[Double], pipeline: Pipeline): BDM[Double] = {
        // Spark setup
        val conf = new SparkConf().setAppName("GibbsDenoiser")
                                    //.setMaster("local[*]")
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        
        val sc = new SparkContext(conf)

        // Split big matrix into submatrixes, according to subHeight and subWidth
        val splitted = splitImage(inputMatrix)

        // Make RDD of matrixes
        val matrixes = sc.parallelize(splitted._1, splitted._2 * splitted._3)
        matrixes.partitionBy(new HashPartitioner(splitted._2 * splitted._3)).persist(StorageLevel.MEMORY_ONLY)
        val computed = compute(matrixes, pipeline)

        // Reassemble the matrix
        val blockMat = new BlockMatrix(computed, subHeight, subWidth)
        val out = Utils.matrixAsBreeze(blockMat.toLocalMatrix())

        // Remove padding border
        out(0 to inputMatrix.rows -1, 0 to inputMatrix.cols -1).copy
    }


    /**
      * Split image matrix into submatrixes based on subHeight and subWidth
      *
      * @param pixelMatrix input matrix
      * @return Seq[((i,j), matrix)]
      */
    private def splitImage(pixelMatrix: BDM[Double]): (Seq[((Int, Int), Matrix)], Int, Int) = {
        val subHeight = if(this.subHeight <= 0) pixelMatrix.rows else this.subHeight
        val subWidth = if(this.subWidth <= 0) pixelMatrix.cols else this.subWidth
        assert(padding <= subHeight)
        assert(padding <= subWidth)
        assert(padding >= 0)
        assert(pixelMatrix.rows >= subHeight)
        assert(pixelMatrix.cols >= subWidth)

        // Calculate matrixs
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

        // Padded matrix
        val paddedMatrix = BDM.zeros[Double](pixelMatrix.rows + restHeight + padding*2, pixelMatrix.cols + restWidth + padding*2)
        // Set padded image
        paddedMatrix(padding to padding + pixelMatrix.rows -1, padding to padding + pixelMatrix.cols -1) := pixelMatrix
        
        if(debug > 0) {
            println("matrix x size: " + paddedMatrix.rows)
            println("matrix y size: " + paddedMatrix.cols)
            println("x sub-matrix: " + n)
            println("y sub-matrix: " + m)
        }
        // For each pair of matrix elements crop the input matrix
        (for { 
            p1 <- (0 until n) // X
            p2 <- (0 until m) // Y
        } yield {
            val xFromPadded = p1 * subWidth
            val xToPadded = xFromPadded + subWidth + padding*2 -1
            val yFromPadded = p2 * subHeight
            val yToPadded = yFromPadded + subHeight + padding*2 -1
            val matrix = paddedMatrix(yFromPadded to yToPadded, xFromPadded to xToPadded).copy
            ((p2, p1), Utils.matrixFromBreeze(matrix))
        }, n, m)
    }

    /**
      * Apply pipeline tasks to the given RDD matrixes
      *
      * @param matrixes spark RDD
      * @param pipeline defined pipeline
      * @return processed RDD
      */
    private def compute(matrixes : RDD[((Int, Int), Matrix)], pipeline: Pipeline): RDD[((Int, Int), Matrix)] = {
        matrixes.map ( element => {
            val matrix = Utils.matrixAsBreeze(element._2)
            val out = removePadding(pipeline.run(matrix))
            (element._1, Utils.matrixFromBreeze(out))
        })
    }

    // Remove pading from the matrix
    def removePadding(matrix: BDM[Double]) : BDM[Double] = 
        matrix(padding to -padding, padding to -padding).copy
}