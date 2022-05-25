import breeze.linalg.DenseMatrix

trait Job {
    var inputImage: String
    var outputImage: String

    var padding: Int
    var subHeight: Int
    var subWidth: Int

    var denoiserRuns: Int

    def main(args: Array[String]): Unit
    def run(): Unit

    def denoise(matrix: DenseMatrix[Double]) =
        new Denoiser( matrix, denoiserRuns ).run()

    def conv(matrix: DenseMatrix[Double], convMatrix: DenseMatrix[Double]) : DenseMatrix[Double] =
        new Convolution( matrix,  convMatrix).run()

    def removePadding(matrix: DenseMatrix[Double]) : DenseMatrix[Double] = 
        matrix(padding to -padding, padding to -padding).copy

    def processPipelne(matrix: DenseMatrix[Double]) = 
        removePadding(
            conv(
                conv(
                    denoise(matrix), 
                    DenseMatrix((0.1, 0.1, 0.1), (0.1, 0.1, 0.1), (0.1, 0.1, 0.1))
                ),
                DenseMatrix((-1.0, -1.0, -1.0), (-1.0, 8.0, -1.0), (-1.0, -1.0, -1.0))
            )
        )
              
}
