package Schach.controller.controllerComponent.controllerBaseImpl

import Schach.controller.controllerComponent.ControllerInterface
import Schach.{AwaitImplicitFutureResult, GameFieldModule}
import com.dimafeng.testcontainers.DockerComposeContainer
import com.google.inject.{Guice, Injector}
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.awt.Color

class ControllerITSpec(container: DockerComposeContainer) extends AnyWordSpec with Matchers with AwaitImplicitFutureResult {

  val injector: Injector = Guice.createInjector(new GameFieldModule)

  val controller : ControllerInterface = injector.getInstance(classOf[ControllerInterface])

  val vec: Vector[Int] = Vector(0, 1, 0, 2)

  "A Controller" when {
    "Waiting for DockerComposeContainer to be ready" in {
      assert(container.getServicePort("mongodb", 27017) > 0)
      assert(container.getServicePort("persistence", 8081) > 0)
      assert(container.getServicePort("model", 8082) > 0)
    }

    "used for updating and creating a GameField via HTTP" should {

      val injector: Injector = Guice.createInjector(new GameFieldModule)

      val controller : ControllerInterface = injector.getInstance(classOf[ControllerInterface])

      "create a GameField" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        val result = for {
          piece <- controller.getGameFieldViaHttp
        } yield piece

        result.isEmpty should be(false)
      }

      "give the first turn to white and the second to black" in {
        for {
          _ <- controller.createGameField()
        } yield succeed
        controller.getPlayer() should be (Color.WHITE)

        val newColor = controller.changePlayer()

        newColor should be (Color.BLACK)
      }

      "set a player correctly" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        val newColor = controller.setPlayer(Color.BLACK)

        newColor should not be Color.WHITE
      }

      "handle undo/redo correctly" in {
        for {
          _ <- controller.createGameField()
        } yield succeed
        
        controller.movePiece(vec)
        waitUntilResult[Figure](() => controller.getGameFieldViaHttp, getVectorAfterMove(vec))
        
        val tmp = controller.gameFieldToString

        controller.undo()
        controller.gameFieldToString should not be tmp

        controller.redo()
        controller.gameFieldToString should be(tmp)
      }

      "save and load a state" in {

        val old = for {piece <- controller.createGameField()} yield piece

        controller.save()

        val moveValid = controller.movePiece(vec)

        moveValid should be(true)

        val movedField = for {piece <- controller.getGameFieldViaHttp} yield piece

        movedField should not be old

        val restoredField = for {piece <- controller.restore()} yield piece

        restoredField should be(old)
      }

      "save a game persistently and load a game save" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        for {
          _ <- controller.saveGame()
        } yield succeed

        val old = controller.gameFieldToString
        val moveValid = controller.movePiece(vec)

        moveValid should be(true)
        for { piece <- controller.getGameFieldViaHttp } yield succeed

        controller.gameFieldToString should not be old

        val saves =
          for {
            save <- controller.listSaves()
          } yield save

        saves.last._2.toString should be(old)
      }

      "load a game from persistence" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        controller.saveGame()

        controller.movePiece(vec)
        val old = controller.gameFieldToString

        for {
          _ <- controller.loadGame()
        } yield succeed
        controller.gameFieldToString should not be old
      }
    }

  }
}
