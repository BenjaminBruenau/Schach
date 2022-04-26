package gameManager.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import akka.http.scaladsl.{Http, server}
import com.typesafe.config.{Config, ConfigFactory}
import gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.figureComponent.Figure

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success, Try}

object GameManagerController {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()

    val host: String = config.getString("http.host")
    val port: String = config.getString("http.port")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val gameFieldBuilder = new ChessGameFieldBuilder

    val route =
      concat(
        path("manager" / "makeGameField") {
          get {
            gameFieldBuilder.makeGameField()
            complete(StatusCodes.OK)
          }
        },
        path("manager" / "getGameField") {
          get {
            gameFieldBuilder.getGameField
            complete(StatusCodes.OK)
          }
        },
        path("manager" / "getNewGameField") {
          get {
            gameFieldBuilder.getNewGameField
            complete(StatusCodes.OK)
          }
        },
        path("manager" / "updateGameField") {
          put {
            entity(as[String]) {
              gameField =>
                gameFieldBuilder.updateGameField(newField = Vector.empty)
                complete(StatusCodes.OK)
            }
          }
        },
      )


    println("Server for GameManager started at http://" + host + ":" + port + "\n Press RETURN to stop...")

    val bindingFuture = Http().newServerAt(host, port.toInt).bind(route)
  }
}
