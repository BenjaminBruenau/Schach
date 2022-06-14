package persistence.daoMockImpl

import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.DAOInterface

import scala.concurrent.Future

class DAOImplSuccess extends DAOInterface {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  private val gameFieldBuilder: ChessGameFieldBuilder = new ChessGameFieldBuilder
  
  def loadGame(saveID: Long): Future[GameField] = Future(gameFieldBuilder.getNewGameField)

  def saveGame(gameField: GameField): Future[Boolean] = 
    if gameField.gameField.isEmpty then Future(false)
    else Future(true)

  def listSaves: Future[Vector[(Long, GameField)]] = Future(Vector((1, gameFieldBuilder.getNewGameField)))
}
