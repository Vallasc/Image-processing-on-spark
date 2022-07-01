package Pipelines

import _root_.Algorithms.{Denoiser, Convolution, Invert}
import breeze.linalg.DenseMatrix

class GibbsDenoiser(denoiserRuns: Int) extends Pipeline (
    List(
        new Denoiser( denoiserRuns )
    )) {}
