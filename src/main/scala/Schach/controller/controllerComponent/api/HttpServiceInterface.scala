package Schach.controller.controllerComponent.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.FutureHandler

import scala.concurrent.Future
import scala.swing.Color

trait HttpServiceInterface extends GameFieldJsonProtocol with SprayJsonSupport {
  val futureHandler: FutureHandler

  def getGameSavesViaHttp: Future[Vector[(Long, GameField)]]

  def loadGameViaHttp(id: Long): Future[GameField]

  def saveGameViaHttp(body: GameField): Future[HttpResponse]

  def makeGameFieldViaHttp: Future[GameField]

  def getGameFieldViaHttp: Future[GameField]

  def updatePlayerViaHttp(newPlayer: Color): Future[GameField]

  def updateFieldViaHttp(newField: Vector[Figure]): Future[GameField]

  def updateStatusViaHttp(newStatus: GameStatus): Future[GameField]

  def updateGameFieldViaHttp(newFieldTuple: (Vector[Figure], GameStatus, Color)): Future[GameField]

  def sendPUT(uri: String, body: String): Future[HttpResponse]

  def sendGET(uri: String): Future[HttpResponse]

}
