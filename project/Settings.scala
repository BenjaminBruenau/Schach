import sbt._

object Settings {
  
  object versions {
    val scalatic = "3.2.11"
    val scalatest = "3.2.11"
    val scalaSwing = "3.0.0"
    val scalaXml = "2.0.1"
    val scalaGuice = "5.0.2"
    val playJson = "2.9.2"
  }

  lazy val scalaVersion = "3.1.1"

  lazy val microServicesBranch = "https://github.com/BenjaminBruenau/Schach.git#SA04-Microservices"
  
  val dependencies = Seq(
    "org.scalactic" %% "scalactic" % versions.scalatic,
    "org.scalatest" %% "scalatest" % versions.scalatest % "test",
    "org.scala-lang.modules" %% "scala-swing" % versions.scalaSwing,
    "org.scala-lang.modules" %% "scala-xml" % versions.scalaXml,
    "net.codingwell" %% "scala-guice" % versions.scalaGuice cross CrossVersion.for3Use2_13,
    "com.typesafe.play" %% "play-json" % versions.playJson cross CrossVersion.for3Use2_13
  )
}