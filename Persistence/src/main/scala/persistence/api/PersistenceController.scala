package persistence.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.google.inject.{Guice, Inject, Injector}
import com.typesafe.config.{Config, ConfigFactory}
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.GameFieldJsonProtocol
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.DAOInterface
import spray.json.*
import persistence.PersistenceModule

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object PersistenceController extends GameFieldJsonProtocol with SprayJsonSupport {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()

    val host: String = config.getString("http.host")
    val port: String = config.getString("http.port")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val injector: Injector = Guice.createInjector(new PersistenceModule)

    val dao = injector.getInstance(classOf[DAOInterface])
    

    val route =
      concat(
        path("persistence" / "save") {
          put {
            entity(as[GameField]) {
              gameField =>
                if (dao.saveGame(gameField))
                  complete {
                    gameField
                  }
                else
                  complete(StatusCodes.BadRequest, "Unable to save JSON GameField")
            }
          }
        },
        // e.g. http://localhost:8081/persistence/load?id=2
        path("persistence" / "load") {
          get {
            parameter("id".as[Long]) {
              id => {
                Try(dao.loadGame(id)) match {
                  case Success(field) =>
                    complete {
                      field
                    }
                  case Failure(_) => complete(StatusCodes.BadRequest, "Invalid JSON GameField")
                }
              }
            }
          }
        },
        path("persistence" / "list") {
          get {
            complete {
              dao.listSaves.toJson
            }
          }
        }
      )

    val bindingFuture = Http().newServerAt(host, port.toInt).bind(route)

    println("Server for Persistence started at http://" + host + ":" + port + "\n Press RETURN to stop...")

    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }


}