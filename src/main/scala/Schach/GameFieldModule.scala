package Schach

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule
import model.gameFieldComponent.gameFieldBaseImpl._
import model.gameFieldComponent._
import model.fileIOComponent._
import controller.controllerComponent._

class GameFieldModule extends AbstractModule {

  val builder = new ChessGameFieldBuilder

  override def configure() : Unit = {
    bind(classOf[ControllerInterface]).to(classOf[controllerBaseImpl.Controller])
    bind(classOf[GameFieldInterface]).toInstance(new GameField(builder.getNewGameField.getFigures))

    //bind[FileIOInterface].to[fileIOXMLImpl.FileIO]
    bind(classOf[FileIOInterface]).to(classOf[fileIOJSONImpl.FileIO])
  }
}
