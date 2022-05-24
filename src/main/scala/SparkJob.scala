import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File
import breeze.linalg.{CSCMatrix => BSM, DenseMatrix => BDM, Matrix => BM}
import org.apache.spark.rdd.RDD

import org.apache.spark.mllib.linalg.{Matrix, Matrices}
import org.apache.spark.mllib.linalg.distributed.BlockMatrix
import java.io.PrintWriter
import java.nio.file.Paths
import java.net.URI

object SparkJob  extends Job {
    var inputImage = new Image(new File("./data/testo_noisy.png"))
    var outputImage = new Image(new File("./data/out.png"))
    var outputJson = new File("./data/report.json")

    var padding = 10
    var subHeight = 300
    var subWidth = 300
    var denoiserRuns = 100

    var debug = 1

    val usage = """
        Usage: [--sub_matrix_size] [--padding] [--denoiser_runs] [--debug] [--output_file_json] [--output_file_image] input_file_image
    """
    def main(args: Array[String]): Unit = {
        // Check arguments
        if (args.length == 0) println(usage)
        args.sliding(2, 2).toList.collect {
            case Array("--sub_matrix_size", m_size: String) => {
                subHeight = m_size.toInt
                subWidth = m_size.toInt
            }
            case Array("--padding", p: String) => padding = p.toInt
            case Array("--denoiser_runs", runs: String) => denoiserRuns = runs.toInt
            case Array("--debug", d: String) => debug = d.toInt
            case Array("--output_file_json", out: String) => outputJson = Paths.get(URI.create(s"$out")).toFile()
            case Array("--output_file_image", out: String) => outputImage = new Image(Paths.get(URI.create(s"$out")).toFile())
            case Array(out: String) => inputImage = new Image(Paths.get(URI.create(s"$out")).toFile())
        }

        println("Start")
        val t = Utils.time(run)
        if(debug > 0)
            println(s"Time: $t ms")

        val pw = new PrintWriter(outputJson)
        pw.write("{\"time\":" + t +"}")
        pw.close
    }

    def run(): Unit = {
        val conf = new SparkConf().setAppName("GibbsDenoiser")
                                    //.setMaster("local[*]")
        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        conf.registerKryoClasses(Array(classOf[Tuple2[Tuple2[Int, Int], Matrix]]))

        val sc = new SparkContext(conf)

        val pixelArray = inputImage.getPixelMatrix(true)
        val pixelMatrix = new BDM[Double](inputImage.width, inputImage.height, pixelArray.map(_.toDouble))

        
        val splitted = splitImage(pixelMatrix)

        var n = (pixelMatrix.cols) / subWidth // cols divisions
        var m = (pixelMatrix.rows) / subHeight // rows divisions

        val mat = sc.parallelize(splitted, n * m * 100)
        val computed = compute(mat, processPipelne)

        val blockMat = new BlockMatrix(computed, subHeight, subWidth)
        val out = Utils.matrixAsBreeze(blockMat.toLocalMatrix())
        val cleaned = out(0 to pixelMatrix.rows -1, 0 to pixelMatrix.cols -1).copy

        outputImage.setPixelMatrix(cleaned.data.map(_.toInt), cleaned.rows, cleaned.cols, true)
        outputImage.saveImage
    }


    private def splitImage(pixelMatrix: BDM[Double]): Seq[((Int, Int), Matrix)] = {
        val subHeight = if(this.subHeight <= 0) pixelMatrix.rows else this.subHeight
        val subWidth = if(this.subWidth <= 0) pixelMatrix.cols else this.subWidth
        assert(padding <= subHeight)
        assert(padding <= subWidth)
        assert(padding >= 0)
        assert(pixelMatrix.rows >= subHeight)
        assert(pixelMatrix.cols >= subWidth)

        var n = (pixelMatrix.cols) / subWidth // cols divisions
        var m = (pixelMatrix.rows) / subHeight // rows divisions
        var restWidth = (pixelMatrix.cols % subWidth)
        var restHeight = (pixelMatrix.rows % subHeight)

        if(restWidth > 0){
            n += 1
            restWidth = subWidth - restWidth
        } 
        if(restHeight > 0){
            m += 1
            restHeight = subHeight - restHeight
        } 

        val paddedMatrix = BDM.zeros[Double](pixelMatrix.rows + restHeight + padding*2, pixelMatrix.cols + restWidth + padding*2)
        // Set padded image
        paddedMatrix(padding to padding + pixelMatrix.rows -1, padding to padding + pixelMatrix.cols -1) := pixelMatrix
        
        if(debug > 0) {
            println("matrix x size: " + paddedMatrix.rows)
            println("matrix y size: " + paddedMatrix.cols)
            println("x sub-matrix: " + n)
            println("y sub-matrix: " + m)
        }
        for { 
            p1 <- 0 until n // X
            p2 <- 0 until m // Y
        } yield {
            val xFromPadded = p1 * subWidth
            val xToPadded = xFromPadded + subWidth + padding*2 -1
            val yFromPadded = p2 * subHeight
            val yToPadded = yFromPadded + subHeight + padding*2 -1
            val matrix = paddedMatrix(yFromPadded to yToPadded, xFromPadded to xToPadded).copy
            ((p2, p1), Utils.matrixFromBreeze(matrix))
        }
    }

    private def compute(matrixes : RDD[((Int, Int), Matrix)], transform: (BDM[Double]) => (BDM[Double])): RDD[((Int, Int), Matrix)] = {
        matrixes.map ( element => {
            val matrix = Utils.matrixAsBreeze(element._2)
            val out = transform(matrix)
            (element._1, Utils.matrixFromBreeze(out))
        })
    }

}
// sbt "runMain SparkJob ./data/nike_noisy.png"
// spark-submit --class SparkJob ./jar/binary.jar ./data/nike_noisy.png