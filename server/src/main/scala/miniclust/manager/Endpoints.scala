package miniclust.manager

import io.circe.generic.auto.*
import miniclust.manager.EndpointsAPI
import miniclust.message.Minio
import miniclust.manager.EndpointsAPI.{LoginForm, loginEndpoint, testEndpoint}
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.netty.*
import sttp.model.*
import scala.util.*

object Endpoints:
  def extractCookie(name: String, cookieHeader: String): Option[String] =
    cookieHeader
      .split(";")
      .map(_.trim)
      .find(_.startsWith(s"$name="))
      .map(_.stripPrefix(s"$name="))

class Endpoints(minio: Minio, jwtSecret: JWT.Secret):

  given JWT.Secret = jwtSecret

  val loginSererEndpoint: ServerEndpoint[Any, Identity] =
    loginEndpoint.serverLogic: l =>
      Tool.mc.authenticate(minio, l.username, l.password) match
        case Tool.mc.AuthenticationResult.Success(u) =>
          val token = JWT.encode(JWT.Token(l.username))
          Right(("", token))
        case Tool.mc.AuthenticationResult.Disabled(u) => Left("User account disabled")
        case Tool.mc.AuthenticationResult.Failed => Left("Incorrect login/password")

  val testServerEndpoint: ServerEndpoint[Any, Identity] =
    testEndpoint.handleSecurity: v =>
      Endpoints.extractCookie("jwt", v).headOption match
        case Some(c) =>
          Try(JWT.decode(c)) match
            case Success(_) => Right("")
            case Failure(exception) => Left(exception.getMessage)
        case None => Left("Please login")
    .handleSuccess: t =>
      v => "youpi"

  val apiEndpoints: List[ServerEndpoint[Any, Identity]] = List(loginSererEndpoint, testServerEndpoint)

  val docEndpoints: List[ServerEndpoint[Any, Identity]] = SwaggerInterpreter()
    .fromServerEndpoints[Identity](apiEndpoints, "manager", "1.0.0")

  val all: List[ServerEndpoint[Any, Identity]] = apiEndpoints ++ docEndpoints

