name := "doto-api"
organization := "com.doto"
version := "1.0.0"

scalaVersion := "3.3.3"

lazy val playVersion    = "3.0.4"
lazy val slickPgVersion = "0.22.2"
lazy val circeVersion   = "0.14.9"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaAppPackaging, DockerPlugin)

resolvers ++= Seq(
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  guice,
  evolutions,
  ws,

  // ── Database ────────────────────────────────────────────────────────────────
  "org.playframework"   %% "play-slick"            % "6.1.0",
  "org.playframework"   %% "play-slick-evolutions"  % "6.1.0",
  "org.postgresql"       % "postgresql"             % "42.7.3",
  "com.github.tminglei" %% "slick-pg"              % slickPgVersion,
  "com.github.tminglei" %% "slick-pg_circe-json"   % slickPgVersion,

  // ── JSON ────────────────────────────────────────────────────────────────────
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,

  // ── Auth ────────────────────────────────────────────────────────────────────
  "com.auth0"  % "java-jwt" % "4.4.0",
  "org.mindrot" % "jbcrypt"  % "0.4",

  // ── Test ────────────────────────────────────────────────────────────────────
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
)

dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.2.0"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

Test / javaOptions += "-Dconfig.file=conf/test.conf"
Test / fork := true

dockerBaseImage    := "eclipse-temurin:17-jre"
dockerExposedPorts := Seq(9000)
dockerUpdateLatest := true
