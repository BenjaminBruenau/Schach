package Schach.aview

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.api.HttpServiceInterface
import Schach.controller.controllerComponent.api.httpServiceMockImpl.HttpService
import Schach.controller.controllerComponent.controllerBaseImpl.Controller
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.inject.{AbstractModule, Guice, Injector}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RestUISpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

  class MockModule extends AbstractModule {
    override def configure() : Unit = {
      bind(classOf[ControllerInterface]).toInstance(new Controller(new HttpService))
      bind(classOf[HttpServiceInterface]).to(classOf[HttpService])
    }
  }

  "A RestUI" should {
    val injector: Injector = Guice.createInjector(new MockModule)
    val controller: ControllerInterface = injector.getInstance(classOf[ControllerInterface])
    val rest = new RestUI(controller)
    lazy val routes = rest.route
    val base = "/controller"

    "expose an Endpoint to create a GameField (GET /createGameField))" in {
      Get(base + "/createGameField") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to get a GameField (GET /getGameField))" in {
      Get(base + "/getGameField") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to move a Piece via Parameters (GET /movePiece))" in {
      Get(base + "/movePiece/A2A4") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to undo a Move (GET /undo))" in {
      Get(base + "/undo") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to redo a Move (GET /redo))" in {
      Get(base + "/redo") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to save a Game State(GET /save))" in {
      Get(base + "/save") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to restore a Game State (GET /restore))" in {
      Get(base + "/restore") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to create a GameSave (GET /saveGame))" in {
      Get(base + "/saveGame") ~> routes ~> check {
        responseAs[String] shouldBe a[String]
      }
    }

    "expose an Endpoint to load a GameSave (GET /loadGame))" in {
      Get(base + "/loadGame") ~> routes ~> check {
        contentType should be (ContentTypes.`application/json`)
        responseAs[String] shouldBe a[String]
      }
    }

  }
}
