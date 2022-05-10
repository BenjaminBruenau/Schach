package Schach.controller.controllerComponent.api

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.controllerBaseImpl.*
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import akka.http.scaladsl.{Http, server}
import com.google.inject.{Guice, Injector}
import com.typesafe.config.{Config, ConfigFactory}
import model.gameFieldComponent.gameFieldBaseImpl.GameField
import persistence.api.GameFieldJsonProtocol
import spray.json._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.swing.Reactor


object ControllerRestController extends GameFieldJsonProtocol with SprayJsonSupport {
  val config: Config = ConfigFactory.load()

  val host: String = config.getString("http.host")
  val port: String = config.getString("http.port")

  val injector: Injector = Guice.createInjector(new GameFieldModule)
  val controller: ControllerInterface = injector.getInstance(classOf[ControllerInterface])

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "actorSystem");
  implicit val executionContext: ExecutionContextExecutor = actorSystem.executionContext



  def getWithoutParameter(routePath: String, body: () => String)(executeAction: () => Unit): Route = {
    path("controller" / routePath) {
      get {
        executeAction()
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, body()))
      }
    }
  }


  def getWithMatcher[A](routePath: String, body: () => String, matcher1: PathMatcher1[A])(executeAction: A => Unit): Route = {
    path("controller" / routePath / matcher1) {
      parameter =>
        get {
          executeAction(parameter)
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, body()))
        }
    }
  }


  def startServer(): Future[Http.ServerBinding] = {
    val route = concat(
      getWithoutParameter( "createGameField", () => getGameFieldAsText)(() => controller.createGameField()),
      getWithoutParameter( "getGameField", () => getGameFieldAsText)(() => ()),
      getWithMatcher("movePiece", () => getGameFieldAsText, moveMatcher)((move: Vector[Int]) => controller.movePiece(move)),
      getWithoutParameter("undo", () => getGameFieldAsText)(() => controller.undo()),
      getWithoutParameter("redo", () => getGameFieldAsText)(() => controller.redo()),
      getWithoutParameter("save", () => getGameFieldAsText)(() => controller.save()),
      getWithoutParameter("restore", () => getGameFieldAsText)(() => controller.restore()),
      getWithoutParameter("saveGame", () => getGameFieldAsText)(() => controller.saveGame()),
      path("controller" / "load") {
        get {
          controller.loadGame()
          complete(HttpEntity(ContentTypes.`application/json`, getGameFieldAsText))
        }
      }
    )

    println("Server for Root Controller started at http://" + host + ":" + port + "\n Press RETURN to stop...")
    Http().newServerAt(host, port.toInt).bind(route)
  }

  private def getGameFieldAsText: String = controller.gameFieldToString.concat("\n").concat(controller.printGameStatus())

  val moveMatcher: PathMatcher1[Vector[Int]] = {
    PathMatcher("[A-H][1-8][A-H][1-8]".r).flatMap {
      path => {
        val input = path.charAt(0).toString.concat(path.charAt(1).toString).concat(" ")
          .concat(path.charAt(2).toString).concat(path.charAt(3).toString)
        Option(controller.readInput(input))
      }
    }
  }

  def sendPUT(uri: String, body: String): Future[HttpResponse] = {
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.PUT,
        uri = uri,
        entity =
          HttpEntity(ContentTypes.`application/json`, body)
      )
    )
  }

  def sendGET(uri: String): Future[HttpResponse] = {
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = uri,
      )
    )
  }

  def main(args: Array[String]): Unit = {
    startServer()
  }


}