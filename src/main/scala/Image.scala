import java.awt.image.{BufferedImage, DataBufferByte}
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.Raster
import java.awt.image.WritableRaster
import java.awt.image.SampleModel
import java.awt.image.DataBufferInt
import java.awt.image.ColorModel

class Image(imageFile: File) {

    var image: BufferedImage = null
    var width: Int = 0
    var height: Int = 0
    var hasAlphaChannel = false
    var pixelMatrix: Array[Int] = null

    def getPixelMatrix(greyScale: Boolean = false) : Array[Int] = {
        image = ImageIO.read(imageFile)
        width = image.getWidth
        height = image.getHeight
        hasAlphaChannel = image.getAlphaRaster != null

        val outMatrix: Array[Int] = new Array[Int](width * height)
        pixelMatrix = new Array[Int](width * height)
        val pixels = image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
        val pixelLength = if (hasAlphaChannel) 4 else 3
        for { i <- 0 until pixels.length/pixelLength } {
            var argb = 0
            val pixel = i * pixelLength
            if(hasAlphaChannel) {
                argb += (pixels(pixel) & 0xff) << 24 // alpha
                argb += (pixels(pixel + 1) & 0xff) // blue
                argb += ((pixels(pixel + 2) & 0xff) << 8) // green
                argb += ((pixels(pixel + 3) & 0xff) << 16) // red
            } else {
                argb += -16777216 // 255
                argb += (pixels(pixel) & 0xff) // blue
                argb += ((pixels(pixel + 1) & 0xff) << 8) // green
                argb += ((pixels(pixel + 2) & 0xff) << 16) // red
            }
            pixelMatrix(i) = argb
            outMatrix(i) = if (greyScale) fromARGBtoGreyScale(argb) else argb
        }
        outMatrix
    }

    def setPixelMatrix(pixelMatrix: Array[Int], width: Int, height: Int, greyScale: Boolean = false) = {
        this.width = width
        this.height = height
        if (greyScale)
            this.pixelMatrix = pixelMatrix.map(pixel => fromGreyToARGB(pixel))
        else
            this.pixelMatrix = pixelMatrix
    }

    def saveImage = {
        val buffer: DataBufferInt = new DataBufferInt(pixelMatrix, pixelMatrix.length)

        val bandMasks: Array[Int] = Array(0xFF0000, 0xFF00, 0xFF, 0xFF000000) // ARGB (yes, ARGB, as the masks are R, G, B, A always) order
        val raster: WritableRaster = Raster.createPackedRaster(buffer, width, height, width, bandMasks, null)

        val cm: ColorModel = ColorModel.getRGBdefault()
        val bufferedImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null)
        bufferedImage.setData(raster)
        ImageIO.write(bufferedImage, "PNG", imageFile)
    }

    // 0.3 * R + 0.6 * G + 0.1 * B
    private def fromARGBtoGreyScale(pixel: Int) : Int = {
        val r = (pixel >> 16) & 0xff
        val g = (pixel >> 8) & 0xff
        val b = (pixel) & 0xff
        (r*0.3 + g*0.6 + b*0.1).toInt
    }

    private def fromGreyToARGB(pixel: Int) : Int = {
        val value = pixel & 0xff
        val a = -16777216
        val r = value << 16
        val g = value << 8
        val b = value
        a | r | g | b
    }
}