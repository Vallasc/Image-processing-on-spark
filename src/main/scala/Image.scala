import java.awt.image.{BufferedImage, DataBufferByte}
import java.io.File
import javax.imageio.ImageIO

class Image {

  def getARGBPixelMatrix(inputFile: File) : Array[Array[Int]] = {
    val image: BufferedImage = ImageIO.read(inputFile)
    val pixels = image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
    val width = image.getWidth
    val height = image.getHeight
    val hasAlphaChannel = image.getAlphaRaster != null

    val result = Array.ofDim[Int](height, width)
    val pixelLength = if (hasAlphaChannel) 4 else 3
    var pixel = 0
    var row = 0
    var col = 0
    println(hasAlphaChannel)
    while(pixel + pixelLength -1 < pixels.length) {
      var argb = 0
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
      result(row)(col) = argb

      col += 1
      if (col == width) {
        col = 0
        row += 1
      }
      pixel += pixelLength
    }
    result
  }

  def getGreyScalePixelMatrix(inputFile: File) : Array[Array[Int]] = {
    val matrix = getARGBPixelMatrix(inputFile)
    var row = 0
    while (row < matrix.length){
      var col = 0
      while(col < matrix(0).length){
        val pixel = fromARGBtoGreyScale(matrix(row)(col))
        //matrix(row)(col) = (pixel * (upperBound - lowerBound) / 255) - lowerBound
        matrix(row)(col) = pixel
        col += 1
      }
      row += 1
    }
    matrix
  }

  def getGreyScalePixelMatrix(pixelMatrix: Array[Array[Int]]) : Array[Array[Int]] = {
    var row = 0
    while (row < pixelMatrix.length){
      var col = 0
      while(col < pixelMatrix(0).length){
        val pixel = fromARGBtoGreyScale(pixelMatrix(row)(col))
        //matrix(row)(col) = (pixel * (upperBound - lowerBound) / 255) - lowerBound
        pixelMatrix(row)(col) = pixel
        col += 1
      }
      row += 1
    }
    pixelMatrix
  }
  // 0.3 * R + 0.6 * G + 0.1 * B
  private def fromARGBtoGreyScale(pixel: Int) : Int = {
    val r = (pixel >> 16) & 0xff
    val g = (pixel >> 8) & 0xff
    val b = (pixel) & 0xff
    (r*0.3 + g*0.6 + b*0.1).toInt
  }

  private def fromGreyToRGB(pixel: Int) : Int = {
    val value = pixel & 0xff
    val r = value << 16
    val g = value << 8
    val b = value
    r | g | b
  }

  def saveImage(outputFile: File, pixelMatrix: Array[Array[Int]], isGrey: Boolean) : Unit = {
    val width = pixelMatrix(0).length
    val height = pixelMatrix.length

    val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB );
    var row = 0
    while(row < height) {
      var col = 0
      while (col < width) {
        val pixel = if( isGrey) fromGreyToRGB(pixelMatrix(row)(col)) else pixelMatrix(row)(col)
        bufferedImage.setRGB(col, row, pixel)
        col += 1
      }
      row += 1
    }

    ImageIO.write(bufferedImage, "PNG", outputFile)
  }
}
