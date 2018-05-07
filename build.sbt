name := "Youshuren"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies := Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.11",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.0",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.11" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0" % Test
)