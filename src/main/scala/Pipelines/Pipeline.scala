package Pipelines

import breeze.linalg.DenseMatrix
import Algorithms.Algorithm
import scala.annotation.tailrec

/**
  * Class that defines an image processing pipeline
  *
  * @param nodes pipeline nodes
  */
class Pipeline(nodes: List[Algorithm]) extends Serializable {
    /**
      * Runs all the pipeline tasks
      *
      * @param inputMatrix unprocessed matrix
      * @param tasks by default are all the nodes
      * @return processed matrix
      */
    final def run (inputMatrix :DenseMatrix[Double], tasks: List[Algorithm] = nodes): DenseMatrix[Double] = {
        tasks match {
            case n::nodes => run( n.run(inputMatrix), nodes)
            case Nil => inputMatrix        
        }
    }
}
