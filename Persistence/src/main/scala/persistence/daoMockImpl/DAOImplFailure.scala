package persistence.daoMockImpl

import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.{DAOInterface, RetryExceptionList}

import scala.concurrent.Future

class DAOImplFailure extends DAOInterface {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val errorMessage = "DB Error"
  private val error = RetryExceptionList(Vector((1, new Throwable(errorMessage))))

  def loadGame(saveID: Long): Future[GameField] = Future.failed(error)

  def saveGame(gameField: GameField): Future[Boolean] = Future.failed(error)

  def listSaves: Future[Vector[(Long, GameField)]] = Future.failed(error)
}
