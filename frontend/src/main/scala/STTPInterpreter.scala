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


import sttp.tapir.client.sttp4.*
import sttp.client4.*
import sttp.tapir.Endpoint
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class STTPInterpreter:

  lazy val sttpInterpreter = SttpClientInterpreter()
  lazy val backend = DefaultFutureBackend()

  def toRequest[I, E, O](e: Endpoint[Option[String], I, E, O, Any])(i: I): Future[O] =
    sttpInterpreter.toSecureRequestThrowErrors(e, None).apply(None).apply(i).send(backend).map: r =>
      r.body
