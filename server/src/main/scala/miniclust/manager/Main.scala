package miniclust.manager

import sttp.model.MediaType
import sttp.shared.*
import sttp.tapir.server.netty.sync.*
import sttp.tapir.files.*
import sttp.tapir.*
import sttp.tapir.server.*

import java.nio.file.Paths
import better.files.*
import io.circe.yaml
import miniclust.message.{MiniClust, Minio}
import io.circe.generic.auto.*



object Configuration:
  import io.circe.derivation
  given derivation.Configuration = derivation.Configuration.default.withDefaults.withoutStrictDecoding.withKebabCaseMemberNames.withKebabCaseConstructorNames

  def read(file: File) =
    val configuration = yaml.parser.parse(file.contentAsString).toTry.get.as[Configuration].toTry.get

    val minio =
      miniclust.message.Minio:
        miniclust.message.Minio.Server(
          url = configuration.minio.url,
          user = configuration.minio.key,
          password = configuration.minio.secret,
          timeout = configuration.minio.timeout,
          insecure = configuration.minio.insecure
        )

    val directory = File(configuration.location.getOrElse(System.getProperty("user.home") + "/.miniclust"))
    val database = db.DB(directory / "db")

    (
      configuration = configuration,
      minio = minio,
      directory = directory,
      port = configuration.port.getOrElse(8080),
      jwt = JWT.Secret(configuration.secret.jwt),
      salt = Salt(configuration.secret.salt),
      database = database
    )

  case class MinioConfiguration(
    url: String,
    key: String,
    secret: String,
    timeout: Int = 20,
    insecure: Boolean = false) derives derivation.ConfiguredCodec

  case class MiniClustConfiguration(
    userGroup: Seq[String] = Seq("user"),
    computeGroup: Seq[String] = Seq("compute")) derives derivation.ConfiguredCodec

  case class Secret(
    jwt: String,
    salt: String) derives derivation.ConfiguredCodec

case class Configuration(
  port: Option[Int] = None,
  location: Option[String] = None,
  minio: Configuration.MinioConfiguration,
  miniclust: Configuration.MiniClustConfiguration,
  secret: Configuration.Secret)

@main def run(configFile: String) =
  java.util.logging.Logger.getGlobal.setLevel(java.util.logging.Level.INFO)

  val staticPath = new java.io.File("server/target/frontend").getAbsolutePath

  def someHtml(jsCall: String) =
    import scalatags.Text.all.*

    html(
      head(
        meta(httpEquiv := "Content-Type", content := "text/html; charset=UTF-8"),
        link(rel := "icon", href := "img/favicon.svg", `type` := "img/svg+xml"),
        link(rel := "stylesheet", `type` := "text/css", href := "css/style.css"),
        link(rel := "stylesheet", `type` := "text/css", href := "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"),
        link(rel := "stylesheet", `type` := "text/css", href := "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.3.0/font/bootstrap-icons.css"),
        script(`type` := "text/javascript", src := "https://cdn.jsdelivr.net/npm/bootstrap.native@5.1.5/dist/bootstrap-native.min.js"),
        Seq(s"main.js").map(jf => script(`type` := "text/javascript", src := s"js/$jf "))
      ),
      body(
        onload := jsCall,
        div(id := "appContainer", cls := "centerColumnFlex")
      )
    )

  val config = Configuration.read(File(configFile))

  given Salt = config.salt
  given JWT.Secret = config.jwt

  config.database.initDB()

  val coordinationBucket = Minio.bucket(config.minio, MiniClust.Coordination.bucketName)

  val indexEndpoint: ServerEndpoint[Any, Identity] =
    endpoint.get
      .in("")
      .out(htmlBodyUtf8)
      .serverLogicSuccess:  _ =>
        someHtml("connection(null);").render

  val jsFrontend = staticFilesGetServerEndpoint[Identity]("js")(staticPath) //, options = FilesOptions.default.copy(defaultFile = Some(List("index.html"))))
  val cssFrontend = staticFilesGetServerEndpoint[Identity]("css")(staticPath) //, options = FilesOptions.default.copy(defaultFile = Some(List("index.html"))))

  val endpoints = Endpoints(config.database, config.minio, config.configuration.miniclust)

  NettySyncServer()
    .port(config.port)
    .addEndpoints(List(indexEndpoint, jsFrontend, cssFrontend))
    .addEndpoints(endpoints.all)
    .startAndWait()

