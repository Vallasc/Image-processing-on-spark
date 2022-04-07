import scala.collection._
import scala.collection.parallel.ForkJoinTaskSupport
import java.io.FileOutputStream

object Example {

    def time[R](block: => R): R = {
        val t0 = System.nanoTime()
        val result = block    // call-by-name
        val t1 = System.nanoTime()
        println("Elapsed time: " + (t1 - t0)/1000000 + "ms")
        result
    }

    def largestPalindrome(xs: GenSeq[Int]): Int = {
        xs.aggregate(Int.MinValue)(
            (largest, n) =>
                if ( n > largest && (n.toString).equals(n.toString.reverse) ) n
                else largest,
            math.max
        )
    }

    class Complex(val a: Double, val b: Double){
        def +(that: Complex) = new Complex(this.a+that.a,this.b+that.b)
        def *(that: Complex) = new Complex(
            this.a*that.a-this.b*that.b,this.a*that.b+that.a*this.b)
        def abs() = Math.sqrt(this.a*this.a + this.b*this.b)
    }

    val fileName = "./scalaimage.pgm"


    def run(n:Int, level:Int) : Unit = {
        val out = new FileOutputStream(fileName)
        out.write(("P5\n"+n+" "+n+"\n255\n").getBytes())
        for (j <- (0 until n*n)){ 
            val x = -2.0 + (j%n)*3.0/n
            val y = -1.5 + (j/n)*3.0/n
            var z = new Complex(0,0)
            val c = new Complex(x,y)
            var i = 0
            while (z.abs < 2 && i < level) {z = z*z + c; i=i+1}
            out.write(255*(level-i)/level)
        }
        out.close()
    }


    def runPar(n:Int, level:Int) : Unit = {
        val out = new FileOutputStream(fileName)
        out.write(("P5\n"+n+" "+n+"\n255\n").getBytes())
        var a=new Array[Int](n * n)
        for (j <- (0 until n*n).par) { 
            val x = -2.0 + (j%n)*3.0/n
            val y = -1.5 + (j/n)*3.0/n
            var z = new Complex(0,0)
            var c = new Complex(x,y)
            var i = 0
            while (z.abs < 2 && i < level) {z = z*z + c; i=i+1}
            a(j) = 255*(level-i)/level 
        }
        for(k <- 0 until n*n) out.write(a(k))
        out.close()
    }


    def main(args: Array[String]) = {
        /*val array = (0 until 100000000).toArray
        val parArray = array.par
        parArray.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(100))
        println(time(largestPalindrome(parArray)))
        println(time(largestPalindrome(array)))*/

        /*println(time(run(1200,50)))
        println(time(runPar(1200,50)))*/

        val v = Vector.range(0, 10)
        v.par.foreach{ e => println(e); Thread.sleep(50) }

    }

}