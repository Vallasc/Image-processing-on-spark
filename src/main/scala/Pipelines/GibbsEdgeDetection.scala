package Pipelines

import _root_.Algorithms.{Denoiser, Convolution, Invert}
import breeze.linalg.DenseMatrix

/**
  * Applies a Gibbs denoiser task and the an edge detection kernel
  * to the given matrix
  *
  * @param denoiserRuns
  */
class GibbsEdgeDetection(denoiserRuns: Int) extends Pipeline (
    List(
        new Denoiser( denoiserRuns ),
        new Convolution( DenseMatrix((-1.0, -1.0, -1.0), (-1.0, 8.0, -1.0), (-1.0, -1.0, -1.0))),
        Invert
    )) {}
