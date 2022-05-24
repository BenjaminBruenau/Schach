package gatling.database


import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import gatling.Request


class DatabaseStressSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8081")


  val stress: ScenarioBuilder = Request.databaseScenario("Testing Database - Stress")

  
  setUp(
    stress.inject(stressPeakUsers(1000).during(1.minute))
  ).protocols(httpProtocol)
}