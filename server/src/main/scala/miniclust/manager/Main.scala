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

    (
      configuration = configuration,
      minio = minio,
      directory = File(configuration.location.getOrElse(System.getProperty("user.home") + "/.miniclust")),
      port = configuration.port.getOrElse(8080),
      jwtSecret = JWT.Secret(configuration.jwt.secret)
    )

  case class MinioConfiguration(
    url: String,
    key: String,
    secret: String,
    timeout: Int = 20,
    insecure: Boolean = false) derives derivation.ConfiguredCodec

  case class JWTConfiguration(secret: String) derives derivation.ConfiguredCodec

case class Configuration(
  port: Option[Int] = None,
  location: Option[String] = None,
  minio: Configuration.MinioConfiguration,
  jwt: Configuration.JWTConfiguration)

@main def run(configFile: String) =
  java.util.logging.Logger.getGlobal.setLevel(java.util.logging.Level.INFO)

  val staticPath = new java.io.File("server/target/frontend").getAbsolutePath

  def someHtml(jsCall: String) =
    import scalatags.Text.all.*

    html(
      head(
        meta(httpEquiv := "Content-Type", content := "text/html; charset=UTF-8"),
        link(rel := "icon", href := "img/favicon.svg", `type` := "img/svg+xml"),
        link(rel := "stylesheet", `type` := "text/css", href := "css/style-connect.css"),
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

  val coordinationBucket = Minio.bucket(config.minio, MiniClust.Coordination.bucketName)

//  val database = db.DB(config.directory)
//  database.initDB()


  val indexEndpoint: ServerEndpoint[Any, Identity] =
    endpoint.get
      .in("")
      .out(htmlBodyUtf8)
      .serverLogicSuccess:  _ =>
        someHtml("connection(null);").render

  val staticFrontend = staticFilesGetServerEndpoint[Identity]("js")(staticPath) //, options = FilesOptions.default.copy(defaultFile = Some(List("index.html"))))

  //directory = Paths.get("target/frontend")

  val endpoints = Endpoints(config.minio, config.jwtSecret)

  NettySyncServer()
    .port(config.port)
    .addEndpoints(List(indexEndpoint, staticFrontend))
    .addEndpoints(endpoints.all)
    .startAndWait()

