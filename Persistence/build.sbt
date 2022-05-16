lazy val branch = "https://github.com/BenjaminBruenau/Schach.git#SA05-REST"

lazy val model = ProjectRef(uri(branch), "model")
lazy val gameManager = ProjectRef(uri(branch), "gameManager")

lazy val persistence = (project in file(".")).dependsOn(model, gameManager).aggregate(model, gameManager).settings(
  name := "Schach-Persistence",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= dependencies
)


lazy val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.11",
  "org.scalatest" %% "scalatest" % "3.2.11" % "test",
  "org.scala-lang.modules" %% "scala-xml" %  "2.1.0",
  "net.codingwell" %% "scala-guice" % "5.0.2" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-stream" % "2.6.19" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-http" % "10.2.9" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.9" cross CrossVersion.for3Use2_13,
  "com.typesafe.slick" %% "slick" % "3.3.3" cross CrossVersion.for3Use2_13,
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3" cross CrossVersion.for3Use2_13,
  "org.postgresql" % "postgresql" % "42.3.5",
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.6.0" cross CrossVersion.for3Use2_13
)