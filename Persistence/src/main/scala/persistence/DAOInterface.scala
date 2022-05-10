package persistence

import model.gameFieldComponent.gameFieldBaseImpl.GameField
import spray.json.JsValue

trait DAOInterface {

  def loadGame(saveID: Long): GameField

  def saveGame(gameField: GameField): Boolean
  
  def listSaves: Vector[(Long, GameField)]

}
