scalaVersion := "2.12.15"

name := "gibbs-image-denoiser"
organization := "io.github.vallasc"
version := "1.0"


libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"

libraryDependencies  ++= Seq(
  "org.scalanlp" %% "breeze" % "1.3",
  "org.scalanlp" %% "breeze-viz" % "1.3"
)

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.0" % "provided"

assembly / assemblyMergeStrategy := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

/*assembly/assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => val oldStrategy = ( assembly/assemblyMergeStrategy).value 
            oldStrategy(x)
}*/