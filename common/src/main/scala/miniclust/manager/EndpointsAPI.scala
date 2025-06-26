package miniclust.manager

import sttp.tapir.*
import sttp.model.*
import sttp.tapir.generic.auto.*

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

  case class LoginForm(username: String, password: String)
  val loginEndpoint: PublicEndpoint[LoginForm, String, (String, String), Any] =
    endpoint.post
      .in("login")
      .in(formBody[LoginForm])
      .errorOut(stringBody)
      .out(header[String](HeaderNames.SetCookie))
      .out(stringBody)

