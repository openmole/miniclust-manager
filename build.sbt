def tapirVersion = "1.11.33"
def circeVersion = "0.14.14"

def commonSettings = Seq(
  scalaVersion := "3.7.2",
  version := "0.1.0-SNAPSHOT",
  organization := "org.openmole.miniclust",
  resolvers += "jitpack" at "https://jitpack.io"
)

lazy val server = (project in file("server")).settings(
  commonSettings,

  Compile / resourceGenerators += Def.taskDyn {
    val jsTask = (frontend / Compile / fastOptJS).map(_.data)
    val htmlTask = (frontend / Compile / resourceDirectory).map(_ / "index.html")
    val cssTask = (frontend / Compile / resourceDirectory).map(_ / "style.css")

    Def.task {
      val jsFile = jsTask.value
      val htmlFile = htmlTask.value
      val cssFile = cssTask.value

      val destDir = target.value / "frontend"
      val destJS = destDir / "main.js"

      IO.createDirectory(destDir)

      val copiedHtml = destDir / htmlFile.getName

      IO.copyFile(jsFile, destJS)
      IO.copyFile(htmlFile, copiedHtml)
      IO.copyFile(cssFile, destDir / cssFile.getName)

      Seq(destJS, copiedHtml)
    }
  },

  //fork := true,

  Seq(
    name := "manager",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.typesafe.slick" %% "slick" % "3.6.1",
      "com.h2database" % "h2" % "2.4.240",
      "org.slf4j" % "slf4j-nop" % "2.0.17",
      "com.softwaremill.ox" %% "core" % "1.0.2",
      "io.circe" %% "circe-generic" % circeVersion,
      //"ch.qos.logback" % "logback-classic" % "1.5.16",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
      "ch.epfl.lamp" %% "gears" % "0.2.0",
      "com.github.pathikrit" %% "better-files" % "3.9.2",
      "com.github.openmole.miniclust" %% "message" % "3b22a09d8c",
      "com.github.jwt-scala" %% "jwt-core" % "11.0.0",
      //"com.lihaoyi" %% "upickle" % "4.1.0",
      "io.github.arainko" %%% "ducktape" % "0.2.9",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.10.2" % Test
    ),
  )
) dependsOn(common)

lazy val common = (project in file("common")).enablePlugins(ScalaJSPlugin).settings(
  commonSettings,
  Seq(
    name := "common",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion
    )
  )
)

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    commonSettings,
    name := "frontend",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client4" % tapirVersion,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-circe" % tapirVersion,
      "com.raquo" %%% "laminar" % "17.2.1",
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion
      //"io.github.cquiroz" %%% "scala-java-time" % "2.2.0",
//      "com.softwaremill.sttp.client3" %%% "circe" % "3.10.2"
    ),
    externalNpm := baseDirectory.value
  )
  .dependsOn(common)


