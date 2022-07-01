package Algorithms

import breeze.linalg.DenseMatrix

trait Algorithm extends Serializable {
    def run (imageMatrix :DenseMatrix[Double]): DenseMatrix[Double]
}
