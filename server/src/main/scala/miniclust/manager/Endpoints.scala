package miniclust.manager

import io.circe.generic.auto.*
import miniclust.manager.EndpointsAPI
import miniclust.message.Minio
import miniclust.manager.EndpointsAPI.{LoginForm, loginEndpoint}
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.netty.*
import sttp.model.*


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

  val apiEndpoints: List[ServerEndpoint[Any, Identity]] = List(loginSererEndpoint)

  val docEndpoints: List[ServerEndpoint[Any, Identity]] = SwaggerInterpreter()
    .fromServerEndpoints[Identity](apiEndpoints, "manager", "1.0.0")

  val all: List[ServerEndpoint[Any, Identity]] = apiEndpoints ++ docEndpoints

