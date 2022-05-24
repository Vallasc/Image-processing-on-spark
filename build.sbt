scalaVersion := "2.12.15"

name := "gibbs-image-denoiser"
organization := "io.github.vallasc"
version := "1.0"

libraryDependencies += "org.scalanlp" %% "breeze" % "1.3"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.2.0" % "provided"
libraryDependencies += "com.google.cloud" % "google-cloud-nio" % "0.123.10"


assembly / assemblyOutputPath := file(s"./jar/binary.jar")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// sbt assembly