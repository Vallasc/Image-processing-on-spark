package Algorithms

import breeze.linalg.DenseMatrix

/**
  * Median filter works by moving through the image pixel by pixel,
  * replacing each value with the median value of neighbouring pixels
  */
object MedianFilter extends Algorithm {

    /**
    * Main run algorithm method, takes a matrix and return a matrix
    *
    * @param imageMatrix unprocessed image matrix
    * @return processed image matrix
    */
    def run (imageMatrix :DenseMatrix[Double]): DenseMatrix[Double] = {
        val outMatrix =  DenseMatrix.zeros[Double](imageMatrix.rows, imageMatrix.cols)
        for {
            i <- 1 until imageMatrix.rows -1
            j <- 1 until imageMatrix.cols -1
        } {
            outMatrix(i, j) = (imageMatrix(i -1 to i +1, j -1 to j +1).copy).data.sortWith(_ < _)(4)
        }
        outMatrix
    }
}