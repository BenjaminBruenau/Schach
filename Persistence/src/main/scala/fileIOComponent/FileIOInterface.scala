package fileIOComponent

import spray.json.JsValue

trait FileIOInterface {

  def loadGame: JsValue

  def saveGame(gameField: JsValue): String

}
