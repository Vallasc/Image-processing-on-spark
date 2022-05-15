import breeze.linalg.DenseMatrix

class Convolution (imageMatrix :DenseMatrix[Double], convolutionKernel :DenseMatrix[Double]) {

    def run () :DenseMatrix[Double]  = {
        assert(convolutionKernel.cols == 3)
        assert(convolutionKernel.rows == 3)
        val outMatrix =  DenseMatrix.zeros[Double](imageMatrix.rows , imageMatrix.cols)
        for {
            i <- 1 until outMatrix.rows -1
            j <- 1 until outMatrix.cols -1
        } outMatrix(i, j) = (imageMatrix(i -1 to i +1, j -1 to j +1)
                                .map(elem => elem.toDouble) *:* convolutionKernel).sum.toInt
        adjustImageOut(outMatrix)
    }

    private def adjustImageOut(matrix: DenseMatrix[Double]) : DenseMatrix[Double] =
        matrix.map( elem => if ( elem > 255 ) 255 else
                            if ( elem < 0 ) 0 else elem)
}
