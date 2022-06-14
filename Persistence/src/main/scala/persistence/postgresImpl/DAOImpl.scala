package persistence.postgresImpl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.typesafe.config.{Config, ConfigFactory}
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.{DAOInterface, FutureHandler}
import spray.json.*
import slick.jdbc.H2Profile.api.*

import java.awt.Color
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class DAOImpl(uri: String) extends DAOInterface with GameFieldJsonProtocol with SprayJsonSupport {

  val futureHandler: FutureHandler = new FutureHandler

  val postgresDatabase = Database.forURL(
    uri,
    user = "schachmeister420",
    password = "schachconnoisseur",
    driver = "org.postgresql.Driver"
  )

  val schemaCreationFuture: Future[Unit] = postgresDatabase.run(
    DBIO.seq(
      Schemas.gameFieldTable.schema.createIfNotExists
    )
  )

  Await.result(schemaCreationFuture, Duration.Inf)


  override def loadGame(saveID: Long): Future[GameField] =
    /*
    futureHandler.resolveFutureNonBlocking(
      postgresDatabase.run(
        Schemas.gameFieldTable.filter(_.id === saveID).result.head).flatMap(result => convertTableResultToGameField(result))
    )
    */
    for {
      save <- futureHandler.resolveFutureNonBlocking(
        postgresDatabase.run(Schemas.gameFieldTable.filter(_.id === saveID).result.head)
      )
      gameField <- convertTableResultToGameField(save)
    } yield gameField

  override def saveGame(gameField: GameField): Future[Boolean] = {
    val gameFieldVector = gameField.gameField
    val currentPlayer = gameField.currentPlayer
    val gameStatus = gameField.status

    val insertGameSave = DBIO.seq(
      Schemas.gameFieldTable += (0, gameFieldVector.toJson, currentPlayer.toJson, gameStatus.toJson)
    )

    futureHandler.resolveFutureNonBlocking(postgresDatabase.run(insertGameSave).transformWith(result => result match
      case Success(_) => Future(true)
      case Failure(exception) =>
        println(exception.toString)
        Future(false)
    ))
  }

  override def listSaves: Future[Vector[(Long, GameField)]] = {
    futureHandler.resolveFutureNonBlocking(
      postgresDatabase.run(Schemas.gameFieldTable.result)
        .flatMap(saves => Future(saves
          .map(save => mapDBSavesToGameField(save._1, save._2, save._3, save._4)).toVector)
        )
    )
  }

  private def mapDBSavesToGameField(saveID: Long, gameVector: JsValue, currentPlayer: JsValue, gameStatus: JsValue): (Long, GameField) =
    (saveID, GameField(gameVector.convertTo[Vector[Figure]], gameStatus.convertTo[GameStatus], currentPlayer.convertTo[Color]))

  private def convertTableResultToGameField(gameSave: (Long, JsValue, JsValue, JsValue)): Future[GameField] = {
    val gameVector = gameSave._2.convertTo[Vector[Figure]]
    val currentPlayer = gameSave._3.convertTo[Color]
    val gameStatus = gameSave._4.convertTo[GameStatus]

    Future(GameField(gameVector, gameStatus, currentPlayer))
  }
}

