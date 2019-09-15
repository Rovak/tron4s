import Dependencies._
import sbt.Keys.publishArtifact

resolvers += Resolver.jcenterRepo

//enablePlugins(JavaAppPackaging)

val projectVersion = "0.0.24-SNAPSHOT"
val scVersion = "2.12.8"

lazy val proto = (project in file("proto"))
  .settings(

    inThisBuild(List(
      organization := "org.rovak",
      scalaVersion := scVersion,
      version      := projectVersion,
    )),

    name := "tron4s-proto",

    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % "3.4.0" % "protobuf",
      "com.google.api.grpc" % "googleapis-common-protos" % "0.0.3" % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    ) ++ grpcDeps,

    PB.protoSources in Compile := Seq(file("proto/protobuf"), file("proto/target/protobuf_external/google/api")),

    PB.includePaths in Compile := Seq(file("proto/protobuf"), file("proto/target/protobuf_external")),

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

//    assemblyMergeStrategy in assembly := {
//      case PathList("META-INF", xs @ _*) =>
//        MergeStrategy.discard
//      case PathList("META-INF", "MANIFEST.MF") =>
//        MergeStrategy.discard
//      case _ =>
//        MergeStrategy.first
//    },
//
//    publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath + "/.m2/repository"))),
//    publishArtifact in (Compile, packageDoc) := false,
//    publishArtifact in packageDoc := false,
    sources in (Compile,doc) := Seq.empty
  )

lazy val root = (project in file("."))
  .aggregate(proto)
  .dependsOn(proto)
  .settings(
//    mainClass in assembly := Some("tron4s.cli.AppCli"),
//    assemblyJarName in assembly := "tron4s.jar",
    inThisBuild(List(
      organization := "org.rovak",
      scalaVersion := scVersion,
      version      := projectVersion,
    )),

    name := "tron4s",

    libraryDependencies ++= Seq(
//      scalaTest % Test,
      specs2 % Test,

      "org.scala-lang.modules" %% "scala-async" % "0.9.7",

      // Tron
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.25",
      "log4j" % "log4j" % "1.2.17",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "commons-codec" % "commons-codec" % "1.11",
      "com.madgag.spongycastle" % "core" % "1.53.0.0",
      "com.madgag.spongycastle" % "prov" % "1.53.0.0",

      "com.cedarsoftware" % "java-util" % "1.8.0",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "org.apache.commons" % "commons-text" % "1.5",
      "org.apache.commons" % "commons-collections4" % "4.0",
      "com.beust" % "jcommander" % "1.72",

      // Data Access
      "com.typesafe.play" %% "play-slick" % "3.0.1",
      "com.typesafe.play" %% "play-json-joda" % "2.6.9",

      "io.monix" %% "monix" % "3.0.0-RC1",
//      "org.jsoup" % "jsoup" % "1.11.3",
      "io.lemonlabs" %% "scala-uri" % "1.1.1",

      "org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

      "com.github.scopt" %% "scopt" % "3.7.0",

      "io.github.novacrypto" % "BIP39" % "0.1.9",
      "io.github.novacrypto" % "BIP32" % "0.0.9",

      "org.bitcoinj" % "bitcoinj-core" % "0.14.7",

      "com.google.inject" % "guice" % "4.2.2",
      "org.web3j" % "core" % "4.2.0",
      "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.0-M4",
      "com.typesafe.play" %% "play-ws-standalone-json" % "2.0.0-M4",

      "com.typesafe" % "config" % "1.3.3",
      "de.vandermeer" % "asciitable" % "0.3.2",

      "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.18",

      "joda-time" % "joda-time" % "2.10.3",

      //  "com.google.code.findbugs" % "jsr305" % "3.0.0",
      //  "com.github.etaty" %% "rediscala" % "1.8.0",
      //  "com.google.guava" % "guava" % "18.0",
      //      "org.iq80.leveldb" % "leveldb" % "0.10",
      //      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
      //      "com.lihaoyi" % "ammonite" % "1.2.1" % "test" cross CrossVersion.full,

    ) ++
      grpcDeps ++
      akkaDeps ++
      circeDependencies ++
      catsDeps ++
      slickPgDeps ++
      slickDeps,

    // Adds additional packages into Twirl
    //TwirlKeys.templateImports += "org.tronscan.controllers._"

    // Adds additional packages into conf/routes
    // play.sbt.routes.RoutesKeys.routesImport += "org.tronscan.binders._"


//    assemblyMergeStrategy in assembly := {
//      case PathList("META-INF", xs @ _*) =>
//        MergeStrategy.discard
//      case PathList("META-INF", "MANIFEST.MF") =>
//        MergeStrategy.discard
//      case "reference.conf" =>
//        MergeStrategy.concat
//      case _ =>
//        MergeStrategy.first
//    },

//    publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath + "/.m2/repository"))),
//    publishArtifact in (Compile, packageDoc) := false,
//    publishArtifact in packageDoc := false,
//    sources in (Compile,doc) := Seq.empty
  )


scalacOptions in Test ++= Seq("-Yrangepos")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification"
)

//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// Publishing
//publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath + "/.m2/repository")))
//publishArtifact in (Compile, packageDoc) := false
//publishArtifact in packageDoc := false
sources in (Compile,doc) := Seq.empty
