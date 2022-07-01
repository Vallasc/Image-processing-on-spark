import breeze.linalg.{CSCMatrix => BSM, DenseMatrix => BDM, Matrix => BM}
import org.apache.spark.mllib.linalg.{Matrix, DenseMatrix, SparseMatrix, Matrices}

object Utils {

    def time[R](block: => R): (R, Long) = {
        val t0 = System.nanoTime()
        val result = block    // call-by-name
        val t1 = System.nanoTime()
        (result, (t1 - t0)/1000000)
    }

    def matrixAsBreeze (matrix: Matrix): BDM[Double] = {
        if (!matrix.isTransposed) {
            new BDM[Double](matrix.numRows, matrix.numCols, matrix.toArray)
        } else {
            val breezeMatrix = new BDM[Double](matrix.numRows, matrix.numCols, matrix.toArray)
            breezeMatrix.t
        }
    }

    /**
     * Creates a Matrix instance from a breeze matrix.
     * @param breeze a breeze matrix
     * @return a Matrix instance
     */
    def matrixFromBreeze(breeze: BM[Double]): Matrix = {
        breeze match {
            case dm: BDM[Double] =>
                new DenseMatrix(dm.rows, dm.cols, dm.data, dm.isTranspose)
            case sm: BSM[Double] =>
                // There is no isTranspose flag for sparse matrices in Breeze
                new SparseMatrix(sm.rows, sm.cols, sm.colPtrs, sm.rowIndices, sm.data)
            case _ =>
                throw new UnsupportedOperationException(
                s"Do not support conversion from type ${breeze.getClass.getName}.")
        }
    }
}
