package Algorithms

import breeze.linalg.DenseMatrix

/**
  * Invert colors of a given matrix
  */
object Invert extends Algorithm {

    /**
    * Main run algorithm method, takes a matrix and return a matrix
    *
    * @param imageMatrix unprocessed image matrix
    * @return processed image matrix
    */
     def run (imageMatrix :DenseMatrix[Double]) :DenseMatrix[Double]  = {
        imageMatrix.map( elem => 255 - elem)
    }
}
