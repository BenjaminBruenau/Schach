lazy val branch = "https://github.com/BenjaminBruenau/Schach.git"
// For a specific branch use: "xxx/Schach.git#SA09-Gatling"
lazy val model = ProjectRef(uri(branch), "model")

lazy val persistence = (project in file(".")).dependsOn(model).aggregate(model).configs(IntegrationTest).settings(
  Defaults.itSettings,
  name := "Schach-Persistence",
  version := "0.1",
  scalaVersion := "3.1.1",
  libraryDependencies ++= dependencies
)


lazy val dependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.12",
  "org.scalatest" %% "scalatest" % "3.2.12" % "it,test",
  "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.8" % "it",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.19" % "test" cross CrossVersion.for3Use2_13,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9" % "test" cross CrossVersion.for3Use2_13,
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