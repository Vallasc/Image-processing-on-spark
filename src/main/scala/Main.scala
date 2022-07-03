import _root_.Utils.FileUtils
import java.nio.charset.Charset
import breeze.linalg.DenseMatrix
import Algorithms.Invert
import Algorithms.MedianFilter
import Algorithms.MeanFilter
import Pipelines.{EdgeDetection, MedianDenoiser, GibbsEdgeDetection, Pipeline}
import Algorithms.Denoiser
import Pipelines.GibbsDenoiser

/**
  * Main entry point
  * 
  * sbt assembly
  * spark-submit --driver-memory 8g --master local[*]  ./jar/binary.jar ./data/nike_noisy.png
  */
object Main {

    var inputPathImage = "./data/input.png"
    var outputPathImage = "./data/out.png"
    var outputPathJson = "./data/report.json"
    var inputPiepeline = "GibbsEdgeDetection"

    var padding = 3
    var subHeight = 100
    var subWidth = 100
    var denoiserRuns = 90
    var debug = 1
    val usage = "\nUsage: [--sub_matrix_size] [--padding] [--denoiser_runs] [--pipeline] [--debug] [--output_file_json] [--output_file_image] input_file_image\n"
    
    def main(args: Array[String]): Unit = {
        // Check arguments
        if (args.length % 2 == 0) {
            println(usage)
            return
        }
        args.sliding(2, 2).toList.collect {
            case Array("--sub_matrix_size", m_size: String) => {
                subHeight = m_size.toInt
                subWidth = m_size.toInt
            }
            case Array("--padding", p: String) => padding = p.toInt
            case Array("--denoiser_runs", runs: String) => denoiserRuns = runs.toInt
            case Array("--debug", d: String) => debug = d.toInt
            case Array("--output_file_json", out: String) => outputPathJson = out
            case Array("--output_file_image", out: String) => outputPathImage = out
            case Array("--pipeline", out: String) => inputPiepeline = out
            case Array(out: String) => inputPathImage = out
        }
        
        println(s"\nInput file: ${inputPathImage}")
        println(s"Output file: ${outputPathImage}")
        println(s"Output json: ${outputPathJson}")
        println(s"Sub matrix size: ${subHeight}")
        println(s"Paddding: ${padding}")

        // Get image as matrix
        val inputStream = FileUtils.getInputStream(inputPathImage)
        val inputImage = new Image()
        val pixelArray = inputImage.getPixelMatrix(inputStream, true)
        inputStream.close()
        val pixelMatrix = new DenseMatrix[Double](inputImage.width, inputImage.height, pixelArray.map(_.toDouble))

        // Define the Spark Job
        val job = new SparkJob(padding, subHeight, subWidth, denoiserRuns, debug)
        // Match the selected pipeline
        val pipeline = inputPiepeline match {
            case "GibbsDenoise" => new GibbsDenoiser(denoiserRuns)
            case "GibbsEdgeDetection" => new GibbsEdgeDetection(denoiserRuns)
            case "EdgeDetection" => EdgeDetection
            case "MedianDenoiser" => MedianDenoiser
        }

        println("\nStart")
        // Runs job and gets execution time
        val result = Utils.time(job.run(pixelMatrix, pipeline))
        if(debug > 0)
            println(s"Time: ${result._2} ms")

        // Saves output image as file
        val outputStream = FileUtils.getOutputStream(outputPathImage)
        val outputImage = new Image()
        outputImage.setPixelMatrix(result._1.data.map(_.toInt), result._1.rows, result._1.cols, true)
        outputImage.saveImage(outputStream)
        outputStream.close()

        // Forges the output report
        val json = s"""{
                "time": ${result._2},
                "inputPiepeline": "${inputPiepeline}",
                "inputPathImage": "${inputPathImage}",
                "outputPathImage": "${outputPathImage}",
                "outputPathJson": "${outputPathJson}",
                "denoiserRuns": ${denoiserRuns},
                "padding": ${padding},
                "subHeight": ${subHeight},
                "subWidth": ${subWidth}
            }"""

        // Saves report to file
        val jsonOutputStream = FileUtils.getOutputStream(outputPathJson)
        jsonOutputStream.write(json.getBytes(Charset.forName("UTF-8")))
        jsonOutputStream.close()
    }
}