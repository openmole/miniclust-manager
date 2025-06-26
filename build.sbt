val tapirVersion = "1.11.33"

def commonSettings = Seq(
  scalaVersion := "3.7.0",
  version := "0.1.0-SNAPSHOT",
  organization := "org.openmole.miniclust"
)

lazy val server = (project in file("server")).settings(
  commonSettings,

  Compile / resourceGenerators += Def.taskDyn {
    val jsTask = (frontend / Compile / fastOptJS).map(_.data)
    val htmlTask = (frontend / Compile / resourceDirectory).map(_ / "index.html")

    Def.task {
      val jsFile = jsTask.value
      val htmlFile = htmlTask.value
      val destDir = target.value / "frontend"
      val destJS = destDir / "main.js"

      IO.createDirectory(destDir)

      val copiedHtml = destDir / htmlFile.getName

      IO.copyFile(jsFile, destJS)
      IO.copyFile(htmlFile, copiedHtml)

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
      "com.h2database" % "h2" % "2.3.232",
      "org.slf4j" % "slf4j-nop" % "2.0.17",
      "io.circe" %% "circe-generic" % "0.14.14",
      //"ch.qos.logback" % "logback-classic" % "1.5.16",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
      "ch.epfl.lamp" %% "gears" % "0.2.0",
      "com.github.pathikrit" %% "better-files" % "3.9.2",
      "org.openmole.miniclust" %% "message" % "1.1-SNAPSHOT",
      "com.github.jwt-scala" %% "jwt-core" % "11.0.0",
      "com.lihaoyi" %% "upickle" % "4.1.0",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.10.2" % Test
    ),
  )
) dependsOn(common)

lazy val common = (project in file("common")).settings(
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
  .settings(
    commonSettings,
    name := "frontend",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-sttp-client4" % "1.11.29",
      "com.raquo" %%% "laminar" % "17.2.1",
      //"io.github.cquiroz" %%% "scala-java-time" % "2.2.0",
      "com.softwaremill.sttp.client3" %%% "circe" % "3.10.2"
    )
  )
  .dependsOn(common)


