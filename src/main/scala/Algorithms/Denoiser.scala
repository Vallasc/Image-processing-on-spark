package Algorithms

import breeze.linalg.DenseMatrix
import breeze.linalg.sum
import breeze.linalg.DenseVector
import breeze.numerics.exp
import breeze.linalg.InjectNumericOps
import scala.util.Random
import java.io.File

class Denoiser (MAX_BURNS :Int = 100, MAX_SAMPLES :Int = 200) extends Algorithm {
    val ITA = 0.9
    val BETA = 2
    val initialization = "same"

    private def energy (Y : DenseMatrix[Double], X: DenseMatrix[Double]) : Double = {
        val N =  Y.rows
        val M = Y.cols
        -1 * sum( X *:* Y ) 
        + sum( Y( 0 until N-1, :: ) *:* Y( 1 to -1, :: ) )
        + sum( Y( ::, 0 until M-1 ) *:* Y( ::, 1 to -1 ) )
    }

    private def sample (i: Int, j: Int, Y: DenseMatrix[Double], X: DenseMatrix[Double]) : Int = {
        val blanket = new DenseVector[Double]( Array(Y(i-1, j), Y(i, j-1), Y(i, j+1), Y(i+1, j), X(i, j)) )
        
        val w = ITA * blanket(-1) + BETA * sum(blanket(0 until 4))
        val prob = 1 / (1 + math.exp(-2*w))
        //val prob = exp( 2 * sum(blanket).toDouble ) / ( 1 + exp( 2 * sum(blanket).toDouble ))
        if (Random.nextDouble < prob) 1 else -1
    }

    def run (imageMatrix :DenseMatrix[Double]): DenseMatrix[Double] = {
        println("Denoiser: working")
        println(s"Initialization : $initialization")
        val X = adjustImageIn(imageMatrix)
        var Y = X.copy
        val N = Y.rows
        val M = Y.cols
        if (initialization == "neg")
            Y = -1.0 *:* Y
        if (initialization == "rand")
            Y = DenseMatrix.tabulate(imageMatrix.rows, imageMatrix.cols){ (i,j) => randomChoice (Array(-1, 1)) }


        var ctr = 0
        for ( _ <- 0 until MAX_BURNS) {
            for { 
                i <- 1 until N-1
                j <- 1 until M-1
            } Y(i,j) = sample(i, j, Y, X)
            ctr += 1
            if( ctr % 10 == 0 ) {
                println(s"Burn-in ${ctr} done!")
                //println(s"Energy: ${energy(Y, X)}")
            }
        }
        println("Denoiser: done")
        adjustImageOut(Y)
    }

    def randomChoice [T] (values: Array[T]) : T = 
        values (Random.nextInt(values.length))

    private def adjustImageIn(matrix: DenseMatrix[Double]) : DenseMatrix[Double] =
        matrix.map( elem => if ( elem > 128 ) 1.0 else -1.0 )

    private def adjustImageOut(matrix: DenseMatrix[Double]) : DenseMatrix[Double] =
        matrix.map( elem => if ( elem < 0.5 ) 0.0 else 255.0 )
}
