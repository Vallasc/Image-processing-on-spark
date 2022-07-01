import java.io.File
import scala.util.Random
import _root_.Utils.FileUtils

object Noiser {
    val PROB = 0.3
    val inputImage = "./data/nike.png"
    val outputImage = "./data/nike_noisy.png"

    def main(args: Array[String]) = {
        println("Start noiser")
        println("Image: " + inputImage)

        val is = FileUtils.getInputStream(inputImage)
        val imageIn = new Image()
        var matrix = imageIn.getPixelMatrix(is, true)
        is.close()

        val out = matrix.map( pixel => {
            if ( Random.nextDouble < PROB ) {
                if(pixel > 128) 0 else 255
            } else pixel
        })

        val os = FileUtils.getOutputStream(outputImage)
        val imageOut = new Image()
        imageOut.setPixelMatrix(out, imageIn.width, imageIn.height, true)
        imageOut.saveImage(os)
        os.close()

        println("End noiser")
    }
}
