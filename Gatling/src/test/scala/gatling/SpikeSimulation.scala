package gatling

import scala.concurrent.duration.*
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.http.Predef.*

class SpikeSimulation extends Simulation {
  val httpProtocol: HttpProtocolBuilder = http
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8080")


  val regular: ScenarioBuilder = scenario("Testing Application - Spike1")
    .exec(Request.execRequestWithoutParameter("createGameField", "/controller/createGameField"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("movePiece", "/controller/movePiece/A2A3"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("movePiece", "/controller/movePiece/A7A5"))
    .pause(1)

  val spike: ScenarioBuilder = scenario("Testing Application - Spike2")
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
    regular.inject(atOnceUsers(2)).andThen(
      spike.inject(atOnceUsers(2000))
    )
  ).protocols(httpProtocol)
}
