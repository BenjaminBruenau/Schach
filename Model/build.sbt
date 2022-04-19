
lazy val gameManager = ProjectRef(uri(Settings.microServicesBranch), "gameManager")

lazy val model = (project in file(".")).dependsOn(gameManager).aggregate(gameManager).settings(
  name := "Schach-Model",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= Settings.dependencies
)