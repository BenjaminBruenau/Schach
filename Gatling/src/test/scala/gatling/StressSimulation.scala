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


  val stress: ScenarioBuilder = scenario("Testing Application - Stress")
    .exec(Request.execRequestWithoutParameter("createGameField", "/controller/createGameField"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("movePiece", "/controller/movePiece/A2A3"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("movePiece", "/controller/movePiece/A7A5"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("saveGame", "/controller/saveGame"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("loadGame", "/controller/loadGame"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("undo", "/controller/undo"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("redo", "/controller/redo"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("getGameField", "/controller/getGameField"))


  setUp(
    //stressPeakUsers(1500).during(1.minute)
    stress.inject(stressPeakUsers(1000).during(1.minute))
  ).protocols(httpProtocol)
}
