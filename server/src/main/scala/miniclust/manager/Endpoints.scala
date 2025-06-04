package miniclust.manager

import io.circe.generic.auto.*
import miniclust.manager.EndpointsAPI
import Library.*
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Endpoints:
  val helloServerEndpoint: ServerEndpoint[Any, Identity] = EndpointsAPI.helloEndpoint.handleSuccess(user => s"Hello ${user.name}")

  val booksListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint.get
    .in("books" / "list" / "all")
    .out(jsonBody[List[Book]])
  
  val booksListingServerEndpoint: ServerEndpoint[Any, Identity] = booksListing.handleSuccess(_ => Library.books)

  val apiEndpoints: List[ServerEndpoint[Any, Identity]] = List(helloServerEndpoint, booksListingServerEndpoint)

  val docEndpoints: List[ServerEndpoint[Any, Identity]] = SwaggerInterpreter()
    .fromServerEndpoints[Identity](apiEndpoints, "manager", "1.0.0")

  val all: List[ServerEndpoint[Any, Identity]] = apiEndpoints ++ docEndpoints

object Library:
  case class Author(name: String)
  case class Book(title: String, year: Int, author: Author)

  val books = List(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
