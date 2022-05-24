package gatling

import scala.concurrent.duration.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder


class LoadSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8080")


  val load: ScenarioBuilder = Request.applicationScenario("Testing Application - Load")

  setUp(
    load.inject(atOnceUsers(2))
  ).protocols(httpProtocol)
}
