package gatling

import scala.concurrent.duration.*
import io.gatling.core.Predef.*
import io.gatling.core.body.Body
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

object Request {

  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8080")


  def execRequestWithoutParameter(requestName: String, requestUrl: String): ChainBuilder = exec(
    http(requestName)
      .get(requestUrl)
  )

  def execRequestWithParameter(requestName: String, requestUrl: String, body: Body): ChainBuilder = exec(
    http(requestName)
      .put(requestUrl)
      .body(body)
  )
}
