lazy val model = ProjectRef(uri(Settings.microServicesBranch), "model")
lazy val gameManager = ProjectRef(uri(Settings.microServicesBranch), "gameManager")

lazy val fileIO = (project in file(".")).dependsOn(model, gameManager).aggregate(model, gameManager).settings(
  name := "Schach-FileIO",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= Settings.dependencies
)