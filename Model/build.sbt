name := "Schach-Model"
version := "0.1"
scalaVersion := "3.1.1"

libraryDependencies ++= dependencies

lazy val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.12",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test"
)
