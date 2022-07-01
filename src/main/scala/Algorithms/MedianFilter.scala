package Algorithms

import breeze.linalg.DenseMatrix

object MedianFilter extends Algorithm {
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