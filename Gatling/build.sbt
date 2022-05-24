
lazy val gatling = (project in file(".")).settings(
  name := "Schach-Gatling-Performance-Tests",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= dependencies
)

lazy val dependencies = Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.7.6",
  "io.gatling" % "gatling-test-framework" % "3.7.6"
)


