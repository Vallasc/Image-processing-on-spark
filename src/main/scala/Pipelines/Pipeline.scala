package Pipelines

import breeze.linalg.DenseMatrix
import Algorithms.Algorithm
import scala.annotation.tailrec

class Pipeline(nodes: List[Algorithm]) extends Serializable {
    final def run (inputMatrix :DenseMatrix[Double], tasks: List[Algorithm] = nodes): DenseMatrix[Double] = {
        tasks match {
            case n::nodes => run( n.run(inputMatrix), nodes)
            case Nil => inputMatrix        
        }
    }
}
