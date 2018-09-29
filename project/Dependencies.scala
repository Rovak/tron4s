import sbt._

object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  val specs2 = "org.specs2" %% "specs2-core" % "4.3.4"


  val circeVersion = "0.9.3"
  val slickPgVersion = "0.16.3"
  val slickVersion = "3.2.3"
  val monixVersion = "2.3.0"
  val akkaVersion = "2.5.16"
  val catsVersion = "1.3.1"
  val grpcVersion = "1.9.0"
  val scaleCubeVersion = "1.0.7"

  val akkaStreamsContribDeps = Seq(
    "com.typesafe.akka" %% "akka-stream-contrib" % "0.8"
  )

  val circeDependencies = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-parser",
    //    "io.circe" %% "circe-scalajs_sjs0.6"
  ).map(_ % circeVersion)

  val akkaDeps = Seq(
    "com.typesafe.akka" %% "akka-actor",
    "com.typesafe.akka" %% "akka-stream",
    "com.typesafe.akka" %% "akka-cluster",
    "com.typesafe.akka" %% "akka-cluster-tools"
  ).map(_ % akkaVersion)

  val slickPgDeps = Seq(
    "com.github.tminglei" %% "slick-pg",
    "com.github.tminglei" %% "slick-pg_play-json",
    "com.github.tminglei" %% "slick-pg_circe-json",
    "com.github.tminglei" %% "slick-pg_joda-time"
  ).map(_ % slickPgVersion)

  val slickDeps = Seq(
    "com.typesafe.slick" %% "slick"
  ).map(_ % slickVersion)

  val catsDeps = Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-macros" % catsVersion,
    "org.typelevel" %% "cats-kernel" % catsVersion,
    "org.typelevel" %% "cats-effect" % "1.0.0"
  )

  val macroParadise = addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

  val scalaAsync = Seq(
    "org.scala-lang.modules" %% "scala-async" % "0.10.0"
  )

  val grpcDeps = Seq(
    "io.grpc" % "grpc-protobuf" % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-stub" % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  )

  val scaleCubeDeps = Seq(
    "io.scalecube" % "scalecube-services",
    "io.scalecube" % "scalecube-cluster",
    "io.scalecube" % "scalecube-transport"
  ).map(_ % scaleCubeVersion)
}
