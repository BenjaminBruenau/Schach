package persistence

import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import spray.json.JsValue
import scala.concurrent.Future

trait DAOInterface {

  def loadGame(saveID: Long): Future[GameField]

  def saveGame(gameField: GameField): Future[Boolean]
  
  def listSaves: Future[Vector[(Long, GameField)]]

}
