package Algorithms

import breeze.linalg.DenseMatrix

/**
  * A convolution process takes a kernel matrix, which is slid across the image and multiplied with the input 
  * such that the output is enhanced in a certain desirable manner.
  *
  * @param convolutionKernel kernel matrix
  */
class Convolution (convolutionKernel :DenseMatrix[Double]) extends Algorithm {

    /**
    * Main run algorithm method, takes a matrix and return a matrix
    *
    * @param imageMatrix unprocessed image matrix
    * @return processed image matrix
    */
    def run (imageMatrix :DenseMatrix[Double]) :DenseMatrix[Double]  = {
        assert(convolutionKernel.cols == 3)
        assert(convolutionKernel.rows == 3)
        val outMatrix =  DenseMatrix.zeros[Double](imageMatrix.rows , imageMatrix.cols)
        for {
            i <- 1 until outMatrix.rows -1
            j <- 1 until outMatrix.cols -1
        } outMatrix(i, j) = (imageMatrix(i -1 to i +1, j -1 to j +1)
                                *:* convolutionKernel).sum.toInt
        adjustImageOut(outMatrix)
    }

    private def adjustImageOut(matrix: DenseMatrix[Double]) : DenseMatrix[Double] =
        matrix.map( elem => if ( elem > 255 ) 255 else
                            if ( elem < 0 ) 0 else elem)
}
