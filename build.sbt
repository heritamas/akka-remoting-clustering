name := "akka-remoting-clustering"

version := "0.1"

scalaVersion := "2.13.14"

lazy val akkaVersion = "2.7.0"
lazy val protobufVersion = "3.6.1"
lazy val aeronVersion = "1.30.0"

javaOptions += "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "io.aeron"           % "aeron-driver" % aeronVersion
)