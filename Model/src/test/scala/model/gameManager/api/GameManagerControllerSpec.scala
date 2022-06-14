package model.gameManager.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import model.gameModel.gameFieldComponent.{GameFieldJsonProtocol, GameStatus}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

import java.awt.Color

class GameManagerControllerSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with GameFieldJsonProtocol with SprayJsonSupport {

  "A GameManagerController" should {
    val gameFieldBuilder = new ChessGameFieldBuilder
    val base = "/manager"
    val rest = GameManagerController()
    lazy val routes = rest.route
    val freshField = gameFieldBuilder.getNewGameField

    "expose an Endpoint to make a GameField (GET /makeGameField)" in {
      Get(base + "/makeGameField") ~> routes ~> check {
        val loadedGameField = responseAs[GameField]
        loadedGameField shouldBe a[GameField]
        loadedGameField shouldEqual freshField
      }
    }

    "expose an Endpoint to get a GameField (GET /getGameField)" in {
      Get(base + "/getGameField") ~> routes ~> check {
        val loadedGameField = responseAs[GameField]
        loadedGameField shouldBe a[GameField]
      }
    }

    "expose an Endpoint to get a NEW GameField (GET /getNewGameField)" in {
      Get(base + "/getNewGameField") ~> routes ~> check {
        val loadedGameField = responseAs[GameField]
        loadedGameField shouldBe a[GameField]
        loadedGameField shouldEqual freshField
      }
    }

    "expose an Endpoint to update the GameField and return the new one (PUT /updateGameField)" should {

      "be able to receive a single Vector[Figure] to update the field" in {
        val gameFieldVector = freshField.moveTo(0, 1, 0, 2)
        val entity = Marshal(gameFieldVector.toJson).to[MessageEntity].futureValue

        Put(base + "/updateGameField").withEntity(entity) ~> routes ~> check {
          val loadedGameField = responseAs[GameField]
          loadedGameField shouldBe a[GameField]
          loadedGameField.gameField shouldEqual gameFieldVector
        }
      }

      "be able to receive a single Color to update the current player" in {
        val entity = Marshal(Color.BLACK).to[MessageEntity].futureValue

        Put(base + "/updateGameField").withEntity(entity) ~> routes ~> check {
          val loadedGameField = responseAs[GameField]
          loadedGameField shouldBe a[GameField]
          loadedGameField.currentPlayer should be (Color.BLACK)
        }
      }

      "be able to receive a single GameStatus to update the current status" in {
        val entity = Marshal(GameStatus.Checked).to[MessageEntity].futureValue

        Put(base + "/updateGameField").withEntity(entity) ~> routes ~> check {
          val loadedGameField = responseAs[GameField]
          loadedGameField shouldBe a[GameField]
          loadedGameField.status should be (GameStatus.Checked)
        }
      }

      "be able to receive a Tuple of all 3 GameField Parameters to update it with" in {
        val gameFieldVector = freshField.moveTo(0, 1, 0, 2)
        val newPlayer = Color.BLACK
        val newStatus = GameStatus.Checked
        val gameFieldTuple = (gameFieldVector, newStatus, newPlayer)
        val entity = Marshal(gameFieldTuple.toJson).to[MessageEntity].futureValue

        Put(base + "/updateGameField").withEntity(entity) ~> routes ~> check {
          val loadedGameField = responseAs[GameField]
          loadedGameField shouldBe a[GameField]
          loadedGameField.status should be (newStatus)
          loadedGameField.currentPlayer should be (newPlayer)
          loadedGameField.gameField shouldEqual gameFieldVector
        }
      }

      "be able to handle an invalid update Parameter" in {
        val entity = Marshal((1, freshField).toJson).to[MessageEntity].futureValue
        Put(base + "/updateGameField").withEntity(entity) ~> routes ~> check {
          status should be(StatusCodes.BadRequest)

          responseAs[String] should be("Invalid Parameters")
        }
      }
    }

  }

}
