scalaVersion := "2.12.15"

name := "gibbs-image-donoiser"
organization := "io.github.vallasc"
version := "1.0"


libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"

libraryDependencies  ++= Seq(
  "org.scalanlp" %% "breeze" % "2.0.1-RC1",
  "org.scalanlp" %% "breeze-viz" % "2.0.1-RC1"
)

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.0"
