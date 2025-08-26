package miniclust.manager

import io.circe.generic.auto.*
import miniclust.manager.EndpointsAPI
import miniclust.message.Minio
import miniclust.manager.EndpointsAPI.{LoginForm, MinioUser, listUser, loginEndpoint, registerEndpoint, testEndpoint}
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.netty.*
import sttp.model.*

import scala.util.*
import io.github.arainko.ducktape.*
import db.DB
import miniclust.manager.Configuration.MiniClustConfiguration
import miniclust.manager.db.DBSchemaV1.ManagerUser.Role

object Endpoints:
  def extractCookie(name: String, cookieHeader: String): Option[String] =
    cookieHeader
      .split(";")
      .map(_.trim)
      .find(_.startsWith(s"$name="))
      .map(_.stripPrefix(s"$name="))

  def verifyCookie(c: Option[String])(using JWT.Secret) =
    c match
      case Some(c) =>
        Try(JWT.decode(c)) match
          case Success(token) => Right(token.id)
          case Failure(exception) => Left(exception.getMessage)
      case None => Left("Please login")

  def verifyUser(db: DB, role: Option[Role] = None)(using JWT.Secret)(c: Option[String]): Either[String, Any] =
    (verifyCookie(c), role) match
      case (Right(id), Some(role)) =>
        db.user(id) match
          case Some(user) if user.role == role => Right(id)
          case _ => Left("User do not have sufficient permission")
      case (e, _) => e

class Endpoints(db: DB, minio: Minio, miniclust: MiniClustConfiguration)(using jwt: JWT.Secret, salt: Salt):

  val loginServerEndpoint: ServerEndpoint[Any, Identity] =
    loginEndpoint.serverLogic: l =>
      db.autenticate(l.username, l.password) match
        case Right(u) =>
          val token = JWT.encode(JWT.Token(u.id))
          Right((s"jwt=$token", ""))
        case Left(_) => Left("Incorrect login/password")

  val listUserServerEndpoint: ServerEndpoint[Any, Identity] =
    listUser.handleSecurity(Endpoints.verifyUser(db)).handleSuccess: _ =>
      _ =>
        MC.userList(minio).
          filter:
            u => u.memberOf.map(_.name).exists(miniclust.userGroup.contains)
          .map: u =>
            MinioUser(
              login = u.accessKey,
              status = u.userStatus.to[MinioUser.Status]
            )


//  val testServerEndpoint: ServerEndpoint[Any, Identity] =
//    testEndpoint.handleSecurity(Endpoints.verifyUser(db)).handleSuccess: t =>
//      v => "youpi"

//  val registerServerEndpoint: ServerEndpoint[Any, Identity] =
//    registerEndpoint.handleSuccess: t =>
//      val registerUser =
//        DB.MiniClustUser(
//          firstName = t.firstName,
//          name = t.name,
//          email = t.email,
//          institution = t.institution,
//          password = Crypto.encrypt(t.password)
//        )
//
//      db.addRegisterUser(registerUser)

  val apiEndpoints: List[ServerEndpoint[Any, Identity]] = List(loginServerEndpoint, listUserServerEndpoint)

  val docEndpoints: List[ServerEndpoint[Any, Identity]] = SwaggerInterpreter()
    .fromServerEndpoints[Identity](apiEndpoints, "manager", "1.0.0")

  val all: List[ServerEndpoint[Any, Identity]] = apiEndpoints ++ docEndpoints

