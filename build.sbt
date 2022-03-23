name := "Schach"

version := "0.1"

scalaVersion := "3.1.1"


target in Compile in doc := baseDirectory.value / "Schach-Docs" / "docs"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.11"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "3.0.0"

//libraryDependencies += "com.google.inject" % "guice" % "4.2.3" cross CrossVersion.for3Use2_13
libraryDependencies += "net.codingwell" %% "scala-guice" % "5.0.2" cross CrossVersion.for3Use2_13

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2" cross CrossVersion.for3Use2_13


