val tapirVersion = "1.11.15"

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
      "ch.qos.logback" % "logback-classic" % "1.5.16",
      "com.lihaoyi" %% "scalatags" % "0.13.1",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.10.2" % Test
    )
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
      //"io.github.cquiroz" %%% "scala-java-time" % "2.2.0",
      "com.softwaremill.sttp.client3" %%% "circe" % "3.10.2"
    )
  )
  .dependsOn(common)


