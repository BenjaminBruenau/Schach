package gatling

import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

class StressSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8080")


  val stress: ScenarioBuilder = Request.applicationScenario("Testing Application - Stress")


  setUp(
    //stressPeakUsers(1500).during(1.minute)
    stress.inject(stressPeakUsers(1000).during(1.minute))
  ).protocols(httpProtocol)
}
