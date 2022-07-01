package Pipelines

import _root_.Algorithms.MedianFilter
import breeze.linalg.DenseMatrix

object MedianDenoiser extends Pipeline (
    List(
        MedianFilter,
        MedianFilter
    )) {}