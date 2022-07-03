package Pipelines

import _root_.Algorithms.{Denoiser, Convolution, Invert}
import breeze.linalg.DenseMatrix

/**
  * Applies a Gibbs denoiser task to the given matrix
  *
  * @param denoiserRuns
  */
class GibbsDenoiser(denoiserRuns: Int) extends Pipeline (
    List(
        new Denoiser( denoiserRuns )
    )) {}
