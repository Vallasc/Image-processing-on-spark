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
case x if Assembly.isConfigFile(x) =>
  MergeStrategy.concat
case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
  MergeStrategy.rename
case PathList("META-INF", xs @ _*) =>
  (xs map {_.toLowerCase}) match {
    case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
      MergeStrategy.discard
    case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
      MergeStrategy.discard
    case "plexus" :: xs =>
      MergeStrategy.discard
    case "services" :: xs =>
      MergeStrategy.filterDistinctLines
    case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
      MergeStrategy.filterDistinctLines
    case _ => MergeStrategy.first
  }
case _ => MergeStrategy.first}

// sbt assembly