package Schach

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule
import model.gameFieldComponent.ChessGameFieldBuilderInterface
import model.gameFieldComponent.*
import model.fileIOComponent.{FileIOInterface, *}
import controller.controllerComponent.*

class GameFieldModule extends AbstractModule {

  val builder = new ChessGameFieldBuilder

  override def configure() : Unit = {
    bind(classOf[ControllerInterface]).to(classOf[controllerBaseImpl.Controller])
    bind(classOf[ChessGameFieldBuilderInterface]).toInstance(builder)

    //bind[FileIOInterface].to[fileIOXMLImpl.FileIO]
    bind(classOf[FileIOInterface]).to(classOf[fileIOJSONImpl.FileIO])
  }
}
