lazy val model = ProjectRef(uri("https://github.com/BenjaminBruenau/Schach.git#SA04-Microservices"), "model")

lazy val gameManager = (project in file(".")).dependsOn(model).aggregate(model).settings(
  name := "Schach-GameManager",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= Settings.dependencies
)