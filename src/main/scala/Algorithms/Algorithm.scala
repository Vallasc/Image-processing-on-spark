package Algorithms

import breeze.linalg.DenseMatrix

/**
  * Trait that define an algorithm for the processing pipeline
  */
trait Algorithm extends Serializable {
    
    /**
    * Main run algorithm method, takes a matrix and return a matrix
    *
    * @param imageMatrix unprocessed image matrix
    * @return processed image matrix
    */
    def run (imageMatrix :DenseMatrix[Double]): DenseMatrix[Double]
}
