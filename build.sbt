name := "Youshuren"

//version := "0.1"

scalaVersion := "2.12.6"
scalacOptions += "-Ypartial-unification"

enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin, GitVersioning)
dockerBaseImage := "openjdk:8-jre-alpine"
dockerRepository := Some("tshy0931")

git.formattedShaVersion := git.gitHeadCommit.value map {
  sha => s"v${sha take 7}"
}

mainClass in Compile := Some("org.youshuren.main.Application")

lazy val doobieVersion = "0.5.2"
lazy val akkaVersion = "2.5.11"
lazy val akkaHttpVersion = "10.1.0"
lazy val catsVersion = "1.1.0"
lazy val catsEffectVersion = "0.10.1"
lazy val pureConfigVersion = "0.9.1"
lazy val scalaTestVersion = "3.0.5"

libraryDependencies := Seq(
  "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  "org.typelevel" %% "cats-core"   % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,

  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion,

  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

  "org.scalatest"     %% "scalatest"         % scalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"      % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)