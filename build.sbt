ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "salt-sec"
  )

ThisBuild / libraryDependencies ++=Seq("com.typesafe.play" %% "play" % "2.8.0")