package miniclust.manager

import sttp.tapir.*
import sttp.model.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*

/*
 * Copyright (C) 2025 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

object EndpointsAPI:

  type SecureEndpoint[I, O] = Endpoint[Option[String], I, String, O, Any]

  case class LoginForm(username: String, password: String)

  val loginEndpoint: PublicEndpoint[LoginForm, String, (String, String), Any] =
    endpoint.post
      .in("login")
      .in(formBody[LoginForm])
      .errorOut(stringBody)
      .out(header[String](HeaderNames.SetCookie))
      .out(stringBody)

  val listUser: SecureEndpoint[Unit, Seq[MinioUser]] =
    endpoint.post
      .in("list-user")
      .securityIn(cookie[Option[String]]("jwt"))
      .out(jsonBody[Seq[MinioUser]])
      .errorOut(stringBody)


//  val userEndpoint: Endpoint[String, String, String, User, Any] =
//    endpoint.get
//      .in("user" / path[String]("login"))
//      .securityIn(header[String](HeaderNames.Cookie))
//      .out(jsonBody[User])
//      .errorOut(stringBody)

  case class RegisterUser(
    name: String,
    firstName: String,
    email: String,
    institution: String,
    password: String)

  val registerEndpoint: PublicEndpoint[RegisterUser, String, Unit, Any] =
    endpoint.post
      .in("login")
      .in(formBody[RegisterUser])
      .errorOut(stringBody)

  val testEndpoint: Endpoint[String, Unit, String, String, Any] =
    endpoint.get
      .in("test")
      .securityIn(header[String](HeaderNames.Cookie))
      .out(stringBody)
      .errorOut(stringBody)


  object MinioUser:
    enum Status:
      case enabled, disabled

  case class MinioUser(login: String, status: MinioUser.Status)

  case class User(
   name: String,
   firstName: String,
   login: String,
   email: String,
   institution: String,
   emailStatus: String,
   created: Long)