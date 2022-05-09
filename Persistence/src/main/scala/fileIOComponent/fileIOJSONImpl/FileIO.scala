package fileIOComponent.fileIOJSONImpl

import fileIOComponent.FileIOInterface
import model.gameFieldComponent.gameFieldBaseImpl.GameField
import spray.json.*

import java.io.*
import scala.io.Source

class FileIO extends FileIOInterface {

  override def loadGame: JsValue = {
    val source = Source.fromFile("save.json")
    val file = source.getLines().mkString
    source.close()
    file.parseJson
  }

  override def saveGame(gameField: JsValue): String = {
    val printWriter = new PrintWriter(new File("save.json"))
    val gameFieldString = gameField.prettyPrint
    println("LOL")
    println(gameFieldString)
    printWriter.write(gameFieldString)
    printWriter.close()
    gameFieldString
  }

  




}
