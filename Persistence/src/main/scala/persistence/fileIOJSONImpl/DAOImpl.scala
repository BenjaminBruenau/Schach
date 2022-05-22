package persistence.fileIOJSONImpl

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import model.gameModel.gameFieldComponent.GameFieldJsonProtocol
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.DAOInterface
import spray.json.*

import java.io.*
import scala.io.Source
import scala.util.{Failure, Success, Try}

class DAOImpl extends DAOInterface with GameFieldJsonProtocol with SprayJsonSupport {

  override def loadGame(saveID: Long): GameField = {
    val source = Source.fromFile("save.json")
    val file = source.getLines().mkString
    source.close()
    file.parseJson.convertTo[GameField]
  }

  override def saveGame(gameField: GameField): Boolean = {
    val printWriter = new PrintWriter(new File("save.json"))
    val gameFieldString = gameField.toJson.prettyPrint

    Try(printWriter.write(gameFieldString)) match {
      case Success(_) =>
        printWriter.close()
        true
      case Failure(_) =>
        printWriter.close()
        false
    }
  }

  override def listSaves: Vector[(Long, GameField)] = Vector((1.toLong, loadGame(1)))

}
