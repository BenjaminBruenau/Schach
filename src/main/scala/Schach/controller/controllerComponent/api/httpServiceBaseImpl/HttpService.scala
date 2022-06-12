package Schach.controller.controllerComponent.api.httpServiceBaseImpl

import Schach.controller.controllerComponent.api.HttpServiceInterface
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{Config, ConfigFactory}
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import persistence.FutureHandler
import spray.json.*

import java.awt.Color
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.swing.Color
import scala.util.{Failure, Success}

class HttpService extends HttpServiceInterface {

  val config: Config = ConfigFactory.load()
  val host: String = config.getString("http.persistenceHost")

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "HTTP_SERVICE");
  implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext

  val futureHandler: FutureHandler = new FutureHandler

  /// PERSISTENCE API CALLS
  def getGameSavesViaHttp: Future[Vector[(Long, GameField)]] = {
    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8081/persistence/list")

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    for {
      response <- responseFuture
      gameFieldSaves <- futureHandler.resolveFutureNonBlocking(Unmarshal(response.entity).to[Vector[(Long, GameField)]])
    } yield gameFieldSaves
  }

  def loadGameViaHttp(id: Long): Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8081/persistence/load?id=" + id)

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  def saveGameViaHttp(body: GameField): Future[HttpResponse] = {
    val requestFuture: Future[HttpResponse] =
      sendPUT(
        "http://" + host + ":8081/persistence/save",
        body.toJson.prettyPrint)

    futureHandler.resolveFutureNonBlocking(requestFuture)
  }


  /// GAME MANAGER API CALLS
  def makeGameFieldViaHttp: Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8082/manager/makeGameField")

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  def getGameFieldViaHttp: Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8082/manager/getGameField")

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  def updatePlayerViaHttp(newPlayer: Color): Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newPlayer.toJson.prettyPrint)

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  def updateFieldViaHttp(newField: Vector[Figure]): Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newField.toJson.prettyPrint)

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  def updateStatusViaHttp(newStatus: GameStatus): Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newStatus.toJson.prettyPrint)

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  def updateGameFieldViaHttp(newFieldTuple: (Vector[Figure], GameStatus, Color)): Future[GameField] = {
    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newFieldTuple.toJson.prettyPrint)

    val responseFuture = futureHandler.resolveFutureNonBlocking(requestFuture)

    forComprehendToGameFieldFuture(responseFuture)
  }

  private def forComprehendToGameFieldFuture(responseFuture: Future[HttpResponse]): Future[GameField] = {
    for {
      response <- responseFuture
      gameField <- futureHandler.resolveFutureNonBlocking(Unmarshal(response.entity).to[GameField])
    } yield gameField
  }

  def sendPUT(uri: String, body: String): Future[HttpResponse] = {
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = uri,
        entity =
          HttpEntity(ContentTypes.`application/json`, body)
      )
    )
  }

  def sendGET(uri: String): Future[HttpResponse] = {
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = uri,
      )
    )
  }

}
