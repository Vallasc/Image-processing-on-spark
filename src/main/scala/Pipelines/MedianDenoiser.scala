package Pipelines

import _root_.Algorithms.MedianFilter
import breeze.linalg.DenseMatrix

/**
  * Pipeline that applies a MedianFilter two times
  */
object MedianDenoiser extends Pipeline (
    List(
        MedianFilter,
        MedianFilter
    )) {}