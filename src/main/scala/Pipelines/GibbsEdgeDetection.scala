package Pipelines

import _root_.Algorithms.{Denoiser, Convolution, Invert}
import breeze.linalg.DenseMatrix

class GibbsEdgeDetection(denoiserRuns: Int) extends Pipeline (
    List(
        new Denoiser( denoiserRuns ),
        new Convolution( DenseMatrix((-1.0, -1.0, -1.0), (-1.0, 8.0, -1.0), (-1.0, -1.0, -1.0))),
        new Invert()
    )) {}
