package Pipelines

import _root_.Algorithms.{Convolution, Invert}
import breeze.linalg.DenseMatrix

/**
  * Applies a kernel to detect edges and then inverts th image
  */
object EdgeDetection extends Pipeline (
    List(
        new Convolution( DenseMatrix((-1.0, -1.0, -1.0), (-1.0, 8.0, -1.0), (-1.0, -1.0, -1.0))),
        Invert
    )) {}