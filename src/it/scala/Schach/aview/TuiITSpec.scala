package Schach.aview

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import com.dimafeng.testcontainers.{DockerComposeContainer, ForAllTestContainer}
import com.google.inject.Guice
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TuiITSpec(container: DockerComposeContainer) extends AnyWordSpec with Matchers {

  "DockerComposeContainer are ready" in {
    assert(container.getServicePort("mongodb", 27017) > 0)
    assert(container.getServicePort("persistence", 8081) > 0)
    assert(container.getServicePort("model", 8082) > 0)
  }

  "A Tui" should {
    val injector = Guice.createInjector(new GameFieldModule)
    val controller = injector.getInstance(classOf[ControllerInterface])
    val tui = new Tui(controller)

    "work correctly on undoing an invalid command and loading an invalid save" in {
      tui.interactWithUser("new")
      val old = controller.gameFieldToString

      tui.interactWithUser("undo")
      controller.gameFieldToString should be(old)

      tui.interactWithUser("redo")
      controller.gameFieldToString should be(old)

      tui.interactWithUser("load")
      controller.gameFieldToString should be(old)
    }

    "create a new GameField on command 'new'" in {
      tui.interactWithUser("new")
      for { _ <- controller.getGameFieldViaHttp} yield succeed
      val stringField = controller.gameFieldToString
      tui.lastOutput should be(stringField)
    }

    "move according to the input" in {
      tui.interactWithUser("move A1 A2")

      controller.moveIsValid(tui.readInput("A2 A3")) should be(true)
      controller.moveIsValid(tui.readInput("A1 A1")) should be(false)

      val old = controller.gameFieldToString
      tui.interactWithUser("move XY ZX")

      controller.gameFieldToString should be(old)

      tui.interactWithUser("machmal XY ZX")

      controller.gameFieldToString should be(old)

      tui.interactWithUser("move A2 A3")

      controller.gameFieldToString should not be old
    }

    "undo and redo a move" in {
      tui.interactWithUser("move B7 A6")

      val old = controller.gameFieldToString

      tui.interactWithUser("undo")
      controller.gameFieldToString should not be old

      tui.interactWithUser("redo")
      controller.gameFieldToString should be (old)
    }

    "save and load a state" in {
      tui.interactWithUser("move B1 A3")
      tui.interactWithUser("save")

      val old = controller.gameFieldToString

      tui.interactWithUser("move A3 B5")
      tui.interactWithUser("load")

      controller.gameFieldToString should be(old)
    }

    "act accordingly to check and checkmate" in {
      tui.interactWithUser("new")
      tui.interactWithUser("move E2 E4")
      tui.interactWithUser("move F7 F5")
      tui.interactWithUser("move A2 A4")
      tui.interactWithUser("move E7 E5")
      tui.interactWithUser("move D1 H5")

      controller.isChecked() should be(true)

      tui.interactWithUser("new")
      tui.interactWithUser("move E2 E4")
      tui.interactWithUser("move F7 F5")
      tui.interactWithUser("move D1 H5")

      controller.isCheckmate() should be(true)

      tui.interactWithUser("new")
      tui.interactWithUser("move E2 E4")
      tui.interactWithUser("move E7 E5")

      controller.isChecked() should be (false)

      tui.interactWithUser("move A2 A4")
      tui.interactWithUser("move F7 F5")
      tui.interactWithUser("move D1 H5")

      controller.isChecked() should be (true)

      tui.interactWithUser("new")
      tui.interactWithUser("move D2 D4")
      tui.interactWithUser("move C7 C5")

      controller.isCheckmate() should be(false)

      tui.interactWithUser("move H2 H4")
      tui.interactWithUser("move D8 A5")

      controller.isCheckmate() should be(true)
    }

    "save and load a savefile" in {
      tui.interactWithUser("new")
      tui.interactWithUser("move H2 H4")
      tui.interactWithUser("move B7 B5")
      val old = controller.gameFieldToString

      tui.interactWithUser("save_game")
      tui.interactWithUser("move C2 C3")
      tui.interactWithUser("move A7 A5")

      controller.gameFieldToString should not be old

      tui.interactWithUser("load_game")
      Thread.sleep(100) // Wait for DB Result

      controller.gameFieldToString should be (old)
    }

    "switch the Pawn when he has reached the other side of the GameField" when {

      "change the Pawn into a Queen after the user specified it" in {
        tui.interactWithUser("new")
        tui.interactWithUser("move G2 G4")
        tui.interactWithUser("move H7 H5")
        tui.interactWithUser("move A7 A6")
        tui.interactWithUser("move G4 H5")
        tui.interactWithUser("move H8 H6")
        tui.interactWithUser("move A2 A3")
        tui.interactWithUser("move H6 C6")
        tui.interactWithUser("move H5 H6")
        tui.interactWithUser("move C6 C5")
        tui.interactWithUser("move H6 H7")
        tui.interactWithUser("move C5 C4")
        tui.interactWithUser("move H7 H8")
        tui.interactWithUser("save_game")

        val old = controller.gameFieldToString
        tui.interactWithUser("switch queen")

        controller.gameFieldToString should not be old
      }

      "change the Pawn into a Rook, Knight or Bishop if the user specified it" in {
        tui.interactWithUser("new")
        tui.interactWithUser("load_game")

        tui.convertPawn("rook")

        tui.interactWithUser("new")
        tui.interactWithUser("load_game")

        tui.convertPawn("knight")

        tui.interactWithUser("new")
        tui.interactWithUser("load_game")

        tui.convertPawn("bishop")

        val output = tui.convertPawn("abc")

        output.contains("Wrong Input") should be(true)
      }
    }
  }
}
