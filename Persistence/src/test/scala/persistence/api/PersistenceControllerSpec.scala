package persistence.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.inject.{AbstractModule, Guice, Injector}
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import persistence.DAOInterface

import java.awt.Color

class PersistenceControllerSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with GameFieldJsonProtocol with SprayJsonSupport {

  class MockModuleSuccess extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[DAOInterface]).to(classOf[persistence.daoMockImpl.DAOImplSuccess])
    }
  }

  class MockModuleFailure extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[DAOInterface]).to(classOf[persistence.daoMockImpl.DAOImplFailure])
    }
  }

  "A PersistenceController" when {
    val error = "DB Error"
    val base = "/persistence"
    val emptyGameField = GameField(Vector.empty, GameStatus.Running, Color.WHITE)


    "producing successfull results from the Backend" should {
      val injector: Injector = Guice.createInjector(new MockModuleSuccess)
      val dao = injector.getInstance(classOf[DAOInterface])
      val rest = PersistenceController(dao)
      lazy val routes = rest.route
      val gameFieldBuilder = new ChessGameFieldBuilder
      val fieldToSave = gameFieldBuilder.getNewGameField

      "expose an Endpoint to load a Game from the Database (GET /load)" in {
        Get(base + "/load?id=1") ~> routes ~> check {
          val loadedGameField = responseAs[GameField]
          loadedGameField shouldBe a[GameField]
        }
      }

      "expose an Endpoint to persistently save a Game to the Database (PUT /save)" in {
        val entity = Marshal(fieldToSave).to[MessageEntity].futureValue
        Put(base + "/save").withEntity(entity) ~> routes ~> check {
          responseAs[GameField] shouldBe a[GameField]
        }
      }

      "have the Endpoint react accordingly to the response of the Database (GET /list)" in {
        val entity = Marshal(emptyGameField).to[MessageEntity].futureValue
        Put(base + "/save").withEntity(entity) ~> routes ~> check {
          status should be(StatusCodes.BadRequest)

          responseAs[String] should be ("Unable to save JSON GameField")
        }
      }

      "expose an Endpoint to list saves" in {
        Get(base + "/list") ~> routes ~> check {
          responseAs[Vector[(Long, GameField)]] shouldBe a[Vector[(Long, GameField)]]
        }
      }
    }

    "producing unsuccessfull results from the Backend" should {
      val injector: Injector = Guice.createInjector(new MockModuleFailure)
      val dao = injector.getInstance(classOf[DAOInterface])
      val rest = PersistenceController(dao)
      lazy val routes = rest.route

      "have the (GET /load) endpoint act accordingly on a BackendFailure" in {
        Get(base + "/load?id=1") ~> routes ~> check {
          status should be(StatusCodes.InternalServerError)

          responseAs[String] should be(error)
        }
      }

      "have the (PUT /save) endpoint act accordingly on a BackendFailure" in {
        val entity = Marshal(emptyGameField).to[MessageEntity].futureValue
        Put(base + "/save").withEntity(entity) ~> routes ~> check {
          status should be(StatusCodes.InternalServerError)

          responseAs[String] should be(error)
        }
      }

      "have the (GET /list) endpoint act accordingly on a BackendFailure" in {
        Get(base + "/list") ~> routes ~> check {
          status should be(StatusCodes.InternalServerError)

          responseAs[String] should be(error)
        }
      }
    }

  }
}
