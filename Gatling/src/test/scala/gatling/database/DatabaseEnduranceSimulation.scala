package gatling.database

import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import gatling.Request

class DatabaseEnduranceSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8081")


  val endurance: ScenarioBuilder = Request.databaseScenario("Testing Database - Endurance")

  setUp(
    endurance.inject(constantUsersPerSec(15).during(2.minute))
  ).protocols(httpProtocol)
}
