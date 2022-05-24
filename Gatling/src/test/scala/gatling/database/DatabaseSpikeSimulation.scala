package gatling.database

import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*
import gatling.Request

class DatabaseSpikeSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8081")


  val regular: ScenarioBuilder = scenario("Testing Database - Spike1")
    .exec(Request.execRequestWithoutParameter("list saves", "/persistence/load?id=1"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("load save", "/persistence/list"))
    .pause(1)

  val spike: ScenarioBuilder = scenario("Testing Application - Spike2")
    .exec(Request.execRequestWithParameter("persist save", "/persistence/save", RawFileBody("save.json")))
    .pause(1)



  setUp(
    regular.inject(atOnceUsers(2)).andThen(
      spike.inject(atOnceUsers(2000))
    )
  ).protocols(httpProtocol)
}
