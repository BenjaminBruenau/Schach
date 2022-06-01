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
import scala.concurrent.Future

class DAOImpl extends DAOInterface with GameFieldJsonProtocol with SprayJsonSupport {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global


  override def loadGame(saveID: Long): Future[GameField] = {
    val source = Source.fromFile("save.json")
    val file = source.getLines().mkString
    source.close()
    Future(file.parseJson.convertTo[GameField])
  }

  override def saveGame(gameField: GameField): Future[Boolean] = {
    val printWriter = new PrintWriter(new File("save.json"))
    val gameFieldString = gameField.toJson.prettyPrint

    Try(printWriter.write(gameFieldString)) match {
      case Success(_) =>
        printWriter.close()
        Future(true)
      case Failure(_) =>
        printWriter.close()
        Future(false)
    }
  }

  override def listSaves: Future[Vector[(Long, GameField)]] =
    val source = Source.fromFile("save.json")
    val file = source.getLines().mkString
    source.close()
    Future(Vector((1.toLong, file.parseJson.convertTo[GameField])))

}
