package Schach.model.fileIOComponent.fileIOXMLImpl

import Schach.controller.controllerComponent.controllerBaseImpl.Controller
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class FileIOSpecXML extends AnyWordSpec with Matchers {

  "The XML FileIO" when {
    "making use of the FileIO Implementation" should {
      val fileIo = new FileIO

      val controller = new Controller
      var gameField = controller.gameField

      "save and load a savefile correctly" in {
        val old = gameField.moveTo(0, 1, 0, 2)
        fileIo.saveGame(controller.gameField)

        val now = gameField.moveTo(1, 6, 1, 4)
        now should not be old

        val (v, _) = fileIo.loadGame
        val field = gameField.addFigures(v)
        field should be (old)
      }
    }
  }

}
