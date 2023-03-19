ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "salt-sec"
  )

val http4sBlazeVersion = "0.23.14"
val http4sVersion = "0.23.18"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-client" % http4sBlazeVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
)

//val scalaCacheVersion = "1.0.0-M6"
//libraryDependencies ++= Seq(
//  "com.github.cb372" %% "scalacache-core" % scalaCacheVersion,
//  "com.github.cb372" %% "scalacache-caffeine" % scalaCacheVersion)

// ---- cache -----
// As the service grows this should be backed by a distributed cache (redis/memcached) - L2 cache.
libraryDependencies ++= Seq (
  "com.github.blemale" %% "scaffeine" % "5.2.1"
)

val circeVersion = "0.14.5"
// ------ Json handler -----
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-circe" % http4sVersion,
  // "io.circe" %% "circe-core" % circeVersion
  // "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)