package gatling

import scala.concurrent.duration.*
import io.gatling.core.Predef.*
import io.gatling.core.body.Body
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder

object Request {

  def applicationScenario(name: String): ScenarioBuilder = scenario(name)
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
  
  def databaseScenario(name: String): ScenarioBuilder = scenario(name)
    .exec(Request.execRequestWithoutParameter("list saves", "/persistence/load?id=1"))
    .pause(1)
    .exec(Request.execRequestWithoutParameter("load save", "/persistence/list"))
    .pause(1)
    .exec(Request.execRequestWithParameter("persist save", "/persistence/save", RawFileBody("save.json")))
    .pause(1)
  
  def execRequestWithoutParameter(requestName: String, requestUrl: String): ChainBuilder = exec(
    http(requestName)
      .get(requestUrl)
  )

  val headers_10: Map[String, String] = Map("Content-Type" -> """application/json""")

  def execRequestWithParameter(requestName: String, requestUrl: String, body: Body): ChainBuilder = exec(
    http(requestName)
      .put(requestUrl)
      .headers(headers_10)
      .body(body)
  )
}
