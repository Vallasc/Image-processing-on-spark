import java.io.File
import scala.util.Random

object Noiser {
    val PROB = 0.3

    def main(args: Array[String]) = {
        println("Start noiser")

        val image = new Image(new File("./data/nike.png"))

        var matrix = image.getPixelMatrix(true)

        matrix = matrix.map( pixel => {
            if ( Random.nextDouble < PROB ) {
                if(pixel > 128) 0 else 255
            } else pixel
        })

        val noisy_image = new Image(new File("./data/nike_noisy.png"))
        noisy_image.setPixelMatrix(matrix, image.width, image.height, true)
        noisy_image.saveImage

        println("End noiser")
    }
}
