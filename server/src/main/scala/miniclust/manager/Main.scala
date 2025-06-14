package miniclust.manager

import sttp.model.MediaType
import sttp.shared.*
import sttp.tapir.server.netty.sync.*
import sttp.tapir.files.*
import sttp.tapir.*
import sttp.tapir.server.*
import java.nio.file.Paths
import better.files.*

case class Config(
  port: Int = 8080,
  location: File = File(System.getProperty("user.home"), ".miniclust"))

@main def run =

  val staticPath = new java.io.File("server/target/frontend").getAbsolutePath

  def someHtml(jsCall: String) =
    import scalatags.Text.all.*

    html(
      head(
        meta(httpEquiv := "Content-Type", content := "text/html; charset=UTF-8"),
        link(rel := "icon", href := "img/favicon.svg", `type` := "img/svg+xml"),
        link(rel := "stylesheet", `type` := "text/css", href := "css/style-connect.css"),
        link(rel := "stylesheet", `type` := "text/css", href := "css/bootstrap.css"),
        link(rel := "stylesheet", `type` := "text/css", href := "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.3.0/font/bootstrap-icons.css"),
        Seq(s"main.js").map(jf => script(`type` := "text/javascript", src := s"js/$jf "))
      ),
      body(
        onload := jsCall,
        div(id := "appContainer", cls := "centerColumnFlex")
      )
    )

  val config = Config()

  val database = db.DB(config.location / "db")
  database.initDB()

  val indexEndpoint: ServerEndpoint[Any, Identity] =
    endpoint.get
      .in("")
      .out(htmlBodyUtf8)
      .serverLogicSuccess:  _ =>
        someHtml("run();").render

  val staticFrontend = staticFilesGetServerEndpoint[Identity]("js")(staticPath) //, options = FilesOptions.default.copy(defaultFile = Some(List("index.html"))))

  //directory = Paths.get("target/frontend")

  NettySyncServer()
    .port(config.port)
    .addEndpoints(List(indexEndpoint, staticFrontend))
    .addEndpoints(Endpoints.all)
    .startAndWait()

