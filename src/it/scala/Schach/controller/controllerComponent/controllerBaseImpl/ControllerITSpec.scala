package Schach.controller.controllerComponent.controllerBaseImpl

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import com.dimafeng.testcontainers.DockerComposeContainer
import com.google.inject.{Guice, Injector}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.awt.Color

class ControllerITSpec(container: DockerComposeContainer) extends AnyWordSpec with Matchers {

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
        controller.getGameField.isEmpty should be(false)
      }

      "give the first turn to white and the second to black" in {
        for {
          _ <- controller.createGameField()
        } yield succeed
        controller.getPlayer() should be (Color.WHITE)

        controller.changePlayer()

        for { _ <- controller.getGameFieldAsync } yield succeed
        controller.getPlayer() should be (Color.BLACK)
      }

      "set a player correctly" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        controller.setPlayer(Color.BLACK)
        for { _ <- controller.getGameFieldAsync } yield succeed

        controller.getPlayer() should not be Color.WHITE
      }

      "handle undo/redo correctly" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        controller.movePiece(vec)
        for { _ <- controller.getGameFieldAsync } yield succeed
        val tmp = controller.gameFieldToString

        controller.undo()
        controller.gameFieldToString should not be tmp

        controller.redo()
        controller.gameFieldToString should be(tmp)
      }

      "save and load a state" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        val old = controller.gameFieldToString

        controller.save()
        for { _ <- controller.getGameFieldAsync } yield succeed

        controller.movePiece(vec)
        for { _ <- controller.getGameFieldAsync } yield succeed

        controller.gameFieldToString should not be old

        for {
          _ <- controller.restore()
        } yield succeed
        controller.gameFieldToString should be(old)
      }

      "save a game persistently and load game saves" in {
        for {
          _ <- controller.createGameField()
        } yield succeed

        controller.saveGame()
        for { _ <- controller.getGameFieldAsync } yield succeed

        val old = controller.gameFieldToString
        controller.movePiece(vec)
        for { _ <- controller.getGameFieldAsync } yield succeed

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
          _ <- controller.saveGame()
        } yield succeed

        controller.movePiece(vec)
        for { _ <- controller.getGameFieldAsync } yield succeed
        val old = controller.gameFieldToString

        for {
          _ <- controller.loadGame()
        } yield succeed
        controller.gameFieldToString should not be old
      }
    }

  }
}
