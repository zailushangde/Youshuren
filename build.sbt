name := "Youshuren"

version := "0.1"

scalaVersion := "2.12.6"
scalacOptions += "-Ypartial-unification"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)
dockerBaseImage := "openjdk:8-jre-alpine"

mainClass in Compile := Some("org.youshuren.main.Application")

lazy val doobieVersion = "0.5.2"
lazy val akkaVersion = "2.5.11"
lazy val akkaHttpVersion = "10.1.0"
lazy val catsVersion = "1.1.0"
lazy val catsEffectVersion = "0.10.1"

libraryDependencies := Seq(
  "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  "org.typelevel" %% "cats-core"   % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,

  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion,

  "org.scalatest"     %% "scalatest"         % "3.0.5"  % Test,
  "com.typesafe.akka" %% "akka-testkit"      % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0" % Test
)