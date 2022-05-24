package gatling

import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

class EnduranceSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8080")


  val endurance: ScenarioBuilder = Request.applicationScenario("Testing Application - Endurance")

  setUp(
    endurance.inject(constantUsersPerSec(15).during(4.minute))
  ).protocols(httpProtocol)
}
