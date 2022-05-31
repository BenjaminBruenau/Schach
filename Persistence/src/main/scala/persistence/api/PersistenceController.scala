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
import persistence.{DAOInterface, PersistenceModule, RetryExceptionList}
import spray.json.*
import concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object PersistenceController extends GameFieldJsonProtocol with SprayJsonSupport {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()

    val host: String = config.getString("http.host")
    val port: String = config.getString("http.port")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "PERSISTENCE")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val injector: Injector = Guice.createInjector(new PersistenceModule)

    val dao = injector.getInstance(classOf[DAOInterface])

    val route =
      concat(
        path("persistence" / "save") {
          put {
            entity(as[GameField]) {
              gameField =>
                onComplete(dao.saveGame(gameField)) {
                  case Success(value) =>
                    if value then
                      complete {gameField}
                    else
                      complete(StatusCodes.BadRequest, "Unable to save JSON GameField")
                  case Failure(exception) =>
                    val lastException = exception.asInstanceOf[RetryExceptionList].list.last
                    complete(StatusCodes.BadRequest, lastException._2.getMessage)
                }
            }
          }
        },
        // e.g. http://localhost:8081/persistence/load?id=2
        path("persistence" / "load") {
          get {
            parameter("id".as[Long]) {
              id => {
                onComplete(dao.loadGame(id)) {
                  case Success(field) =>
                    complete {
                      field
                    }
                  case Failure(exception) =>
                    val lastException = exception.asInstanceOf[RetryExceptionList].list.last
                    complete(StatusCodes.BadRequest, lastException._2.getMessage)
                }
              }
            }
          }
        },
        path("persistence" / "list") {
          get {
            onComplete(dao.listSaves) {
              case Success(saves) => complete {
                saves.toJson
              }
              case Failure(exception) =>
                val lastException = exception.asInstanceOf[RetryExceptionList].list.last
                complete(StatusCodes.BadRequest, lastException._2.getMessage)
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