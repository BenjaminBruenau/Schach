package Schach

import Schach.controller.controllerComponent.*
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import fileIOComponent.*
import gameManager.ChessGameFieldBuilderInterface
import gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import net.codingwell.scalaguice.ScalaModule

class GameFieldModule extends AbstractModule {

  val builder = new ChessGameFieldBuilder

  override def configure() : Unit = {
    bind(classOf[ControllerInterface]).to(classOf[controllerBaseImpl.Controller])
    bind(classOf[ChessGameFieldBuilderInterface]).toInstance(builder)

    bind(classOf[FileIOInterface]).to(classOf[fileIOJSONImpl.FileIO])
  }
}
