package Schach

import Schach.controller.controllerComponent.*
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule

class GameFieldModule extends AbstractModule {


  override def configure() : Unit = {
    bind(classOf[ControllerInterface]).to(classOf[controllerBaseImpl.Controller])

  }
}
