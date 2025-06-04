package miniclust.manager

import io.circe.generic.auto.*
import org.openmole.miniclust.Endpoints.{*, given}
import org.openmole.miniclust.Library.*
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.*
import sttp.client3.circe.*
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.stub.TapirStubInterpreter

class EndpointsSpec extends AnyFlatSpec with Matchers with EitherValues:

  it should "return hello message" in {
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub.synchronous)
      .whenServerEndpointRunLogic(helloServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/hello?name=adam")
      .send(backendStub)

    // then
    response.body.value shouldBe "Hello adam"
  }

  it should "list available books" in {
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub.synchronous)
      .whenServerEndpointRunLogic(booksListingServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/books/list/all")
      .response(asJson[List[Book]])
      .send(backendStub)

    // then
    response.body.value shouldBe books
  }
