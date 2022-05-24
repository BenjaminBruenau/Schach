package Schach.controller.controllerComponent.controllerBaseImpl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.{Config, ConfigFactory}
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration
import scala.swing.Color
import scala.util.{Failure, Success}
import spray.json.*

import java.awt.Color

class HttpService extends GameFieldJsonProtocol with SprayJsonSupport {

  val config: Config = ConfigFactory.load()

  val host: String = config.getString("http.persistenceHost")

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "service_actorSystem");
  implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext

  def loadGameViaHttp(id: Long): GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "GET_GAME")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8081/persistence/load?id=" + id)

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
  }

  def saveGameViaHttp(body: GameField) = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "PUT_GAME")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val requestFuture: Future[HttpResponse] =
      sendPUT(
        "http://" + host + ":8081/persistence/save",
        body.toJson.prettyPrint)

    val future = requestFuture.andThen {
      case Success(response) => println("Successfully Saved Game")
      case Failure(exception) => println("Error while resolving Save Game Request")
    }
    Await.ready(future, Duration.Inf)
  }

  def getGameSavesViaHttp: Vector[(Long, GameField)] = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "GET_SAVES")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8081/persistence/list")

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[Vector[(Long, GameField)]]

    Await.result(unmarshalFuture, Duration.Inf)
  }


  def makeGameField: GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "MAKE_GAMEFIELD")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8082/manager/makeGameField")

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
  }

  def getGameField: GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "GET_GAMEFIELD")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val requestFuture: Future[HttpResponse] =
      sendGET("http://" + host + ":8082/manager/getGameField")

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
  }


  def updatePlayer(newPlayer: Color): GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "updatePlayer")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext


    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newPlayer.toJson.prettyPrint)

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
  }

  def updateField(newField: Vector[Figure]): GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "updateField")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext


    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newField.toJson.prettyPrint)

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
  }

  def updateStatus(newStatus: GameStatus): GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "updateStatus")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext


    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newStatus.toJson.prettyPrint)

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
  }

  def updateGameField(newFieldTuple: (Vector[Figure], GameStatus, Color)): GameField = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "updateStatus")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext


    val requestFuture: Future[HttpResponse] =
      sendPUT("http://" + host + ":8082/manager/updateGameField", newFieldTuple.toJson.prettyPrint)

    val response = Await.result(requestFuture, Duration.Inf)

    val unmarshalFuture = Unmarshal(response.entity).to[GameField]

    Await.result(unmarshalFuture, Duration.Inf)
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
