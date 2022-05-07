import sbt._

object Settings {

  object versions {
    val scalatic = "3.2.11"
    val scalatest = "3.2.11"
    val scalaSwing = "3.0.0"
    val scalaXml = "2.1.0"
    val scalaGuice = "5.0.2"
    val playJson = "2.9.2"
    val akkaHttp = "10.2.9"
    val akka = "2.6.19"
  }





  lazy val microServicesBranch = "https://github.com/BenjaminBruenau/Schach.git#SA05-REST"

  val dependencies = Seq(
    "org.scalactic" %% "scalactic" % versions.scalatic,
    "org.scalatest" %% "scalatest" % versions.scalatest % "test",
    "org.scala-lang.modules" %% "scala-swing" % versions.scalaSwing,
    "org.scala-lang.modules" %% "scala-xml" % versions.scalaXml,
    "net.codingwell" %% "scala-guice" % versions.scalaGuice cross CrossVersion.for3Use2_13,
    "com.typesafe.play" %% "play-json" % versions.playJson cross CrossVersion.for3Use2_13,
    "com.typesafe.akka" %% "akka-actor-typed" % versions.akka cross CrossVersion.for3Use2_13,
    "com.typesafe.akka" %% "akka-stream" % versions.akka cross CrossVersion.for3Use2_13,
    "com.typesafe.akka" %% "akka-http" % versions.akkaHttp cross CrossVersion.for3Use2_13,
  )
}
