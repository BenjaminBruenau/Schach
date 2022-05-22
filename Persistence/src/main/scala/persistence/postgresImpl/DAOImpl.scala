package persistence.postgresImpl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.typesafe.config.{Config, ConfigFactory}
import gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.figureComponent.Figure
import model.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import model.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.DAOInterface
import spray.json.*
import slick.jdbc.H2Profile.api.*

import java.awt.Color
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class DAOImpl extends DAOInterface with GameFieldJson with SprayJsonSupport {

  val config: Config = ConfigFactory.load()

  val host: String = config.getString("http.postgresHost")

  val postgresDatabase = Database.forURL(
    "jdbc:postgresql://" + host + ":5432/schachdb",
    user = "schachmeister420",
    password = "schachconnoisseur",
    driver = "org.postgresql.Driver"
  )

  postgresDatabase.run(
    DBIO.seq(
      Schemas.gameFieldTable.schema.create
    )
  )


  override def loadGame(saveID: Long): GameField = {
    val result = Await.result(postgresDatabase.run(Schemas.gameFieldTable.filter(_.id === saveID).result), Duration.Inf)
    val gameSave = result.head

    val gameVector = gameSave._2.convertTo[Vector[Figure]]
    val currentPlayer = gameSave._3.convertTo[Color]
    val gameStatus = gameSave._4.convertTo[GameStatus]

    GameField(gameVector, gameStatus, currentPlayer)
  }

  override def saveGame(gameField: GameField): Boolean = {
    val gameFieldVector = gameField.gameField
    val currentPlayer = gameField.currentPlayer
    val gameStatus = gameField.status

    val insertGameSave = DBIO.seq(
      Schemas.gameFieldTable += (0, gameFieldVector.toJson, currentPlayer.toJson, gameStatus.toJson)
    )

    var result: Boolean = false
    val future = postgresDatabase.run(insertGameSave).andThen {
      case Success(_) => result = true
      case Failure(exception) =>
        println(exception.toString)
        result = false
    }
    Await.ready(future, Duration.Inf)
    result
  }

  override def listSaves: Vector[(Long, GameField)] = {
    val saves = Await.result(postgresDatabase.run(Schemas.gameFieldTable.result), Duration.Inf)

    saves.map(save => mapDBSavesToGameField(save._1, save._2, save._3, save._4)).toVector
  }

  private def mapDBSavesToGameField(saveID: Long, gameVector: JsValue, currentPlayer: JsValue, gameStatus: JsValue): (Long, GameField) =
    (saveID, GameField(gameVector.convertTo[Vector[Figure]], gameStatus.convertTo[GameStatus], currentPlayer.convertTo[Color]))

}
/*
@main def test() = {
  val builder = new ChessGameFieldBuilder
  builder.makeGameField()

  val dao = new DAOImpl
  println(dao.saveGame(builder.getGameField))
  println(dao.loadGame(2).toString)
  dao.listSaves.foreach(save => {
    println(save._1)
    println(save._2)
  })
}
*/
