target in Compile in doc := baseDirectory.value / "Schach-Docs" / "docs"

lazy val model = project in file("Model")
lazy val persistence = (project in file("Persistence")).dependsOn(model)
lazy val gatling = project in file("Gatling")

lazy val schachRoot =
  (project in file(".")).dependsOn(model, persistence).aggregate(model, persistence).configs(IntegrationTest).settings(
    Defaults.itSettings,
    name := "Schach",
    version := "0.1",
    scalaVersion := "3.1.1",
    libraryDependencies ++= dependencies,
    coverageExcludedPackages := ".*gui.*"
  )

lazy val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.12",
  "org.scalatest" %% "scalatest" % "3.2.12" % "it,test",
  "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.8" % "it",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.19" % "it,test" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9" % "it,test" cross CrossVersion.for3Use2_13,
  "org.scala-lang.modules" %% "scala-xml" %  "2.1.0",
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  "net.codingwell" %% "scala-guice" % "5.0.2" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-stream" % "2.6.19" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-http" % "10.2.9" cross CrossVersion.for3Use2_13,
  "ch.qos.logback" % "logback-classic" % "1.2.11"
)

