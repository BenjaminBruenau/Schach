package model.gameManager.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import akka.http.scaladsl.{Http, server}
import com.typesafe.config.{Config, ConfigFactory}
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import spray.json.*

import java.awt.Color
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success, Try}


class GameManagerController extends GameFieldJsonProtocol with SprayJsonSupport {

  private val gameFieldBuilder = new ChessGameFieldBuilder
  
  val route: Route =
    concat(
      path("manager" / "makeGameField") {
        get {
          complete {
            gameFieldBuilder.makeGameField()
          }
        }
      },
      path("manager" / "getGameField") {
        get {
          complete {
            gameFieldBuilder.getGameField
          }
        }
      },
      path("manager" / "getNewGameField") {
        get {
          complete {
            gameFieldBuilder.getNewGameField
          }
        }
      },
      path("manager" / "updateGameField") {
        put {
          entity(as[JsValue]) {
            json =>
              Try(json.convertTo[(Vector[Figure], GameStatus, Color)]) match
                case Success(gameFieldTuple) =>
                  complete {
                    gameFieldBuilder.updateGameField(gameFieldTuple._1, gameFieldTuple._2, gameFieldTuple._3)
                  }
                case Failure(_) =>
                  Try(json.convertTo[Vector[Figure]]) match
                    case Success(gameFieldVector) =>
                      complete {
                        gameFieldBuilder.updateGameField(newField = gameFieldVector)
                      }
                    case Failure(_) => Try(json.convertTo[Color]) match
                      case Success(newPlayer) =>
                        complete {
                          gameFieldBuilder.updateGameField(newPlayer = newPlayer)
                        }
                      case Failure(_) => Try(json.convertTo[GameStatus]) match
                        case Success(newStatus) =>
                          complete {
                            gameFieldBuilder.updateGameField(newStatus = newStatus)
                          }
                        case Failure(exception) =>
                          println(exception.getMessage)
                          complete(StatusCodes.BadRequest, "Invalid Parameters")
          }
        }
      },
    )
}

object GameManagerServer {

  @main def GameManagerMain(): Unit = {
    val config: Config = ConfigFactory.load()

    val host: String = config.getString("http.host")
    val port: String = config.getString("http.port")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
    
    val api: GameManagerController = GameManagerController()

    val route: Route = api.route
    
    val bindingFuture = Http().newServerAt(host, port.toInt).bind(route)

    println("Server for GameManager started at http://" + host + ":" + port + "\n Press RETURN to stop...")

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
