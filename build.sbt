target in Compile in doc := baseDirectory.value / "Schach-Docs" / "docs"

lazy val model = (project in file("Model"))
lazy val fileIO = (project in file("FileIO"))
lazy val gameManager = (project in file ("GameManager"))

lazy val schachRoot = (project in file(".")).dependsOn(model, fileIO, gameManager).aggregate(model, fileIO, gameManager).settings(
  name := "Schach",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= Settings.dependencies
)

