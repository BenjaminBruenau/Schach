package fileIOComponent.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.typesafe.config.{Config, ConfigFactory}
import fileIOComponent.*
import gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameFieldComponent.gameFieldBaseImpl.GameField
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object FileIOController extends GameFieldJsonProtocol with SprayJsonSupport {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()

    val host: String = config.getString("http.host")
    val port: String = config.getString("http.port")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val jsonFileIO = new fileIOJSONImpl.FileIO
    

    val route =
      concat(
        //JSON
        path("fileIO" / "save") {
          put {
            entity(as[GameField]) {
              gameField =>
                jsonFileIO.saveGame(gameField.toJson)
                Try(jsonFileIO.loadGame) match {
                  case Success(field) => complete {
                    field
                  }
                  case Failure(_) => complete(StatusCodes.BadRequest, "Invalid JSON GameField provided")
                }
            }
          }
        },
        path("fileIO" / "load") {
          get {
            Try(jsonFileIO.loadGame) match {
              case Success(field) =>
                complete {
                  field.convertTo[GameField]
              }
              case Failure(_) => complete(StatusCodes.BadRequest, "Invalid JSON GameField")
            }

          }
        },
      )


    println("Server for FileIO started at http://" + host + ":" + port + "\n Press RETURN to stop...")

    val bindingFuture = Http().newServerAt(host, 8081).bind(route)
  }


}