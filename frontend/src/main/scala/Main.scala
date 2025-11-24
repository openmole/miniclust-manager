import org.scalajs.*

import scala.scalajs.js.annotation.JSExportTopLevel
import com.raquo.laminar.api.L.*
import sttp.tapir.client.sttp4.*
import sttp.client4.*
import miniclust.manager.EndpointsAPI
import miniclust.manager.EndpointsAPI.MiniClustUser

import scala.concurrent.ExecutionContext.Implicits.*

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


enum Form:
  case SignIn(error: Option[String] = None) extends Form
  case User

@JSExportTopLevel("connection")
def connection(error: String) = page(Form.SignIn(Option(error)))

@JSExportTopLevel("run")
def run() =
  def renderContent = div("youpi")
  lazy val appContainer = dom.document.querySelector("#appContainer")
  render(appContainer, renderContent)

def page(initialForm: Form) =

  val backend = DefaultFutureBackend()
  val displayedForm: Var[Form] = Var(initialForm)

  def signInForm(error: Option[String]) =
    val login = Var[String]("")
    val password = Var[String]("")

    val connectButton = button("Connect", cls := "btn btn-primary", `type` := "submit", float.right, right := "0")

    div(marginTop := "120", cls := "d-flex justify-content-center align-items-center vh-100",
      div(cls:="d-flex flex-column align-items-end",
        form(
          method := "POST",
          //action := "login",
          onSubmit.preventDefault --> { _ =>
            val formData = EndpointsAPI.LoginForm(login.now(), password.now())
            val loginRequest = SttpClientInterpreter().toRequest(EndpointsAPI.loginEndpoint, None)
            loginRequest(formData).send(backend).foreach: r =>
              if r.code.isSuccess
              then displayedForm.set(Form.User)
              else
                r.body.map:
                  case Left(errorBody) => displayedForm.set(Form.SignIn(Some(errorBody)))
                  case _ =>
          },

          Element.buildInput("Login").amend(
            cls := "form-control mb-2",
            controlled(value <-- login.signal, onInput.mapToValue --> login.writer)
          ),
          Element.buildInput("Password").amend(
            cls := "form-control mb-2",
            `type` := "password",
            controlled(value <-- password.signal, onInput.mapToValue --> password.writer)
          ),
          connectButton
        ),
  //      button("Sign Up", cls := "linkLike", onClick --> { _ => displayedForm.set(Form.SignUp) }),
  //      button("Lost Password", cls := "linkLike", onClick --> { _ => displayedForm.set(Form.AskPasswordReset) }),
        div(error.getOrElse(""), cls := "inputError", minWidth := "0", marginTop := "10")
      )
    )


  def userRow(u: MiniClustUser) =
    tr(
      td(u.login, HTML.centerCell),
      td(s"${u.firstName.getOrElse("NA")} ${u.name.getOrElse("NA")}", HTML.centerCell),
      td(u.email.getOrElse("NA"), HTML.centerCell),
      td(u.institution.getOrElse("NA"), HTML.centerCell),
      td(u.status.toString, HTML.centerCell),
      td(a("Edit"), a("Delete"))
    )

  def userForm =
    table(cls := "table",
      tr(Seq("Login", "Name", "Email", "Institution", "Status", "Action").map(v => th(HTML.centerCell, v))),
      tbody(
        children <--
          Signal.fromFuture(STTPInterpreter().toRequest(EndpointsAPI.listUser)(()), Seq()).map: users =>
            users.map: u =>
              userRow(u)
      )
    )

  val renderContent =
    div(
      //img(src := "img/logo.png", Css.openmoleLogo),
        child <--
          Signal.fromFuture(STTPInterpreter().toPublicRequest(EndpointsAPI.testEndpoint)(()), "").map: r =>
            div(r),
        child <--
          displayedForm.signal.map:
            case s: Form.SignIn => signInForm(s.error)
            case Form.User => userForm
      )

  lazy val appContainer = dom.document.querySelector("#appContainer")

  render(appContainer, renderContent)
