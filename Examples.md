### Collection examples

````scala
// reduceLeft - left associative reduction
def sum(xs: List[Int]): Int =
    (0 :: xs) reduceLeft ((x, y) => x + y)

def sum(xs: List[Int]): Int =
    xs.foldLeft (0) ((x, y) => x + y)

def product(xs: List[Int]): Int =
    (1 :: xs) reduceLeft ((x, y) => x * y)

// scanLeft
// val xs = List(1,3,8)
// xs.scanLeft(0)(_ + _) => List(0, 1, 4, 12)
def sumScanLeft(xs: List[Int]): List[Int] =
    xs.scanLeft(0)(_ + _)

// scanRight
// val xs = List(1,3,8)
// xs.scanRight(0)(_ + _) => List(12, 11, 8, 0)
def sumScanLeft(xs: List[Int]): List[Int] =
    xs.scanRight(0)(_ + _)

def square(xs: List[Int]): List[Int] =
    xs.map(a => a * a)

def scalarProduct(xs: Vector[Double], ys: Vector[Double]) : Double =
    (for ((x, y) <- xs zip ys) yield x * y).sum

def isPrime(n: Int): Boolean =
    (2 until n) forall (d => (n%d != 0))

// For
// compute the set of pairs of integers between 1
// and N having a sum which is prime 
//  (1 to 10) flatMap
//      (i => (1 to i) map (j => (i, j))) filter
//      (pair => isPrime(pair._1 + pair._2)) 
def primes : List[(Int, Int)] =
    for {
        i <- 1 to 10
        j <- 1 to i
        if isPrime (i + j)
    } yield (i, j)

// groupBy
// val donuts: Seq[(String,Double)] = 
//    Seq(("Plain Donut",2.5), ("Strawberry Donut",4.2), ("Glazed Donut",3.3),
//    ("Plain Donut",2.8), ("Glazed Donut",3.1) )
// donuts groupBy (_._1)
//  => Map(Glazed Donut -> List((Glazed Donut,3.3), (Glazed Donut,3.1)),
//      Plain Donut -> List((Plain Donut,2.5), (Plain Donut,2.8)),
//      Strawberry Donut -> List((Strawberry Donut,4.2)))

// Compute the second prime number in the interval
// between 1000 and 10000
def secondPrime = 
    ((1000 to 10000).toStream filter isPrime)(1)


// List(1,3,8).foldLeft(100)((s,x) => s - x) ==
//      ((100 - 1) - 3) - 8 == 88
// List(1,3,8).foldRight(100)((s,x) => s - x) ==
//      1 - (3 – (8 - 100)) == -94
// List(1,3,8).reduceLeft((s,x) => s - x) ==
//      (1 - 3) - 8 == -10
// List(1,3,8).reduceRight((s,x) => s - x) ==
//      1 - (3 - 8) == 6


````
### Parallel
<code>foldLeft</code> is intrinsically sequential, due to the use of the accumulator 

For parallel higher-order programming, fold-like operations are replaced by alternative functions like <code>aggregate</code> 

````scala
def aggregate[B](z: =>B)(seqop: (B, A) => B, combop: (B, B) => B): B
````
If you want to be able to be parallel,
* <code>reduceLeft</code>/<code>reduceRight</code> -> <code>reduce</code> (operations must be commutative and associative)
* <code>foldLeft</code>/<code>foldRight</code> -> <code>aggregate</code>

The order in which numbers are selected for operation by the reduce method is random. This is the reason why non-commutative and non-associative operations are not preferred. 


<iframe src="https://superruzafa.github.io/visual-scala-reference/reduce" width = "100%" height = "100%"></iframe>

{% include "git+https://github.com/blah/button-color.git/README.md" %}
