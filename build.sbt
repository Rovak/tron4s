import Dependencies._

resolvers += Resolver.jcenterRepo

enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).
  settings(
    mainClass in assembly := Some("tron4s.cli.App"),
    assemblyJarName in assembly := "tron4s.jar",
    inThisBuild(List(
      organization := "com.tronweb",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "tronweb4s",

    libraryDependencies ++= Seq(
      scalaTest % Test,
      specs2 % Test,
      "com.google.protobuf" % "protobuf-java" % "3.4.0" % "protobuf",
      "com.google.api.grpc" % "googleapis-common-protos" % "0.0.3" % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",

      "org.scala-lang.modules" %% "scala-async" % "0.9.6",

      // Tron
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.25",
      "log4j" % "log4j" % "1.2.17",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "commons-codec" % "commons-codec" % "1.11",
      "com.madgag.spongycastle" % "core" % "1.53.0.0",
      "com.madgag.spongycastle" % "prov" % "1.53.0.0",
      //  "com.google.guava" % "guava" % "18.0",
      "org.iq80.leveldb" % "leveldb" % "0.10",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
      "org.apache.commons" % "commons-collections4" % "4.0",
      "com.typesafe" % "config" % "1.3.2",
      //  "com.google.code.findbugs" % "jsr305" % "3.0.0",
      "com.cedarsoftware" % "java-util" % "1.8.0",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "org.apache.commons" % "commons-collections4" % "4.0",
      "com.beust" % "jcommander" % "1.72",
      //  "com.github.etaty" %% "rediscala" % "1.8.0",
      "joda-time" % "joda-time" % "2.3",

      // Data Access
      "com.typesafe.play" %% "play-slick" % "3.0.1",
      "com.typesafe.play" %% "play-json-joda" % "2.6.9",

      "io.monix" %% "monix" % "3.0.0-RC1",
      "org.jsoup" % "jsoup" % "1.11.3",
      "io.lemonlabs" %% "scala-uri" % "1.1.1",

      "org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",

//      "com.lihaoyi" % "ammonite" % "1.2.1" % "test" cross CrossVersion.full,

      "com.github.scopt" %% "scopt" % "3.7.0",

      "io.github.novacrypto" % "BIP39" % "0.1.9",

      "io.github.novacrypto" % "BIP32" % "0.0.9",

      "org.bitcoinj" % "bitcoinj-core" % "0.14.7"

      ) ++ grpcDeps ++ akkaDeps ++ circeDependencies ++ catsDeps,

    scalacOptions in Test ++= Seq("-Yrangepos"),

    // Adds additional packages into Twirl
    //TwirlKeys.templateImports += "org.tronscan.controllers._"

    // Adds additional packages into conf/routes
    // play.sbt.routes.RoutesKeys.routesImport += "org.tronscan.binders._"

    PB.protoSources in Compile := Seq(file("src/protobuf"), file("target/protobuf_external/google/api")),

    PB.includePaths in Compile := Seq(file("src/protobuf"), file("target/protobuf_external")),

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),

    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) =>
        MergeStrategy.discard
      case PathList("META-INF", "MANIFEST.MF") =>
        MergeStrategy.discard
      case _ =>
        MergeStrategy.first
    }
)

scalacOptions += "-Ypartial-unification"
