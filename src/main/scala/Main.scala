import java.io.File

object Main extends App {
  println("Hello, World!")

  val image = new Image()
  val matrix = image.getGreyScalePixelMatrix(new File("./data/prova.png"))
  //val matrix = image.getARGBPixelMatrix(new File("./data/prova.png"))
  matrix.foreach(r => {
    r.foreach(c => {
      print(c + " ")
    })
    println()
  })
  image.saveImage(new File("./data/out.png"), matrix, true)
  println("OK")
}
