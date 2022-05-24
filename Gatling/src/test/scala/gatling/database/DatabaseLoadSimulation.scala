package gatling.database

import scala.concurrent.duration.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import gatling.Request


class DatabaseLoadSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8081")


  val load: ScenarioBuilder = Request.databaseScenario("Testing Database - Load")

  setUp(
    load.inject(atOnceUsers(2))
  ).protocols(httpProtocol)
}
