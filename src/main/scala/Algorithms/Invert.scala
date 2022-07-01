package Algorithms

import breeze.linalg.DenseMatrix

class Invert extends Algorithm {
     def run (imageMatrix :DenseMatrix[Double]) :DenseMatrix[Double]  = {
        imageMatrix.map( elem => 255 - elem)
    }
}
