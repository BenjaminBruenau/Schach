package Schach

import Schach.controller.controllerComponent.api.{HttpServiceInterface, httpServiceBaseImpl}
import Schach.controller.controllerComponent.api.httpServiceBaseImpl.HttpService
import Schach.controller.controllerComponent.{ControllerInterface, controllerBaseImpl}
import Schach.controller.controllerComponent.controllerBaseImpl.Controller
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule

class GameFieldModule extends AbstractModule {
  
  override def configure() : Unit = {
    bind(classOf[ControllerInterface]).toInstance(new Controller(new HttpService))

    bind(classOf[HttpServiceInterface]).to(classOf[httpServiceBaseImpl.HttpService])

  }
}
