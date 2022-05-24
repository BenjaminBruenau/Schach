package gatling

import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

class VolumeSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8080")


  val volume: ScenarioBuilder = Request.applicationScenario("Testing Application - Volume")

  setUp(
    volume.inject(atOnceUsers(500))
  ).protocols(httpProtocol)
}
