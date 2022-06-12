package Schach.controller.controllerComponent.api.httpServiceMockImpl

import Schach.controller.controllerComponent.api.HttpServiceInterface
import akka.http.scaladsl.model.HttpResponse
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.GameStatus
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.FutureHandler

import scala.concurrent.Future
import scala.swing.Color

class HttpService extends HttpServiceInterface {
  val futureHandler: FutureHandler = new FutureHandler
  val gameFieldBuilder: ChessGameFieldBuilder = new ChessGameFieldBuilder

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def getGameSavesViaHttp: Future[Vector[(Long, GameField)]] = Future(Vector((1, gameFieldBuilder.getNewGameField)))

  def loadGameViaHttp(id: Long): Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def saveGameViaHttp(body: GameField): Future[HttpResponse] = Future(HttpResponse.apply())

  def makeGameFieldViaHttp: Future[GameField] = Future(gameFieldBuilder.getNewGameField)
 
  def getGameFieldViaHttp: Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def updatePlayerViaHttp(newPlayer: Color): Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def updateFieldViaHttp(newField: Vector[Figure]): Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def updateStatusViaHttp(newStatus: GameStatus): Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def updateGameFieldViaHttp(newFieldTuple: (Vector[Figure], GameStatus, Color)): Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def sendPUT(uri: String, body: String): Future[HttpResponse] = Future(HttpResponse.apply())

  def sendGET(uri: String): Future[HttpResponse] = Future(HttpResponse.apply())
}
