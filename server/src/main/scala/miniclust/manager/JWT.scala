package miniclust.manager

import java.time.Clock
import pdi.jwt.{Jwt, JwtAlgorithm, JwtHeader, JwtClaim, JwtOptions}
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.auto.*

object JWT:
  given clock: Clock = Clock.systemUTC

  object Secret:
    given Conversion[Secret, String] = identity
    def apply(s: String): Secret = s

  opaque type Secret = String
  
  object Json:
    object key:
      val id = "id"

  def algorithm = JwtAlgorithm.HS256
  def oneMounth = 60 * 60 * 24 * 30

  def isTokenValid(token: String)(using secret: Secret): Boolean =
    Jwt.isValid(token, secret, Seq(algorithm))

  def encode(id: Token)(using secret: Secret) =
    Jwt.encode(JwtClaim(id.asJson.noSpaces).issuedNow.expiresIn(oneMounth), secret, algorithm)

  def decode(token: String)(using secret: Secret) =
    parser.parse:
      Jwt.decodeRawAll(token, secret, Seq(algorithm)).get._2
    .toTry.get.as[Token].toTry.get
  
  case class Token(id: String)