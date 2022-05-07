target in Compile in doc := baseDirectory.value / "Schach-Docs" / "docs"

lazy val model = project in file("Model")
lazy val fileIO = project in file("FileIO")
lazy val gameManager = project in file ("GameManager")

lazy val schachRoot = (project in file(".")).dependsOn(model, fileIO, gameManager).aggregate(model, fileIO, gameManager).settings(
  name := "Schach",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= dependencies
)

lazy val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.11",
  "org.scalatest" %% "scalatest" % "3.2.11" % "test",
  "org.scala-lang.modules" %% "scala-xml" %  "2.1.0",
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  "net.codingwell" %% "scala-guice" % "5.0.2" cross CrossVersion.for3Use2_13,
  "com.typesafe.play" %% "play-json" % "2.9.2" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-stream" % "2.6.19" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-http" % "10.2.9" cross CrossVersion.for3Use2_13
)

