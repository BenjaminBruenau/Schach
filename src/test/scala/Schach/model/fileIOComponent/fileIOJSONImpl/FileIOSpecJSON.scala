package Schach.model.fileIOComponent.fileIOJSONImpl

import Schach.controller.controllerComponent.controllerBaseImpl.Controller
import org.scalatest.DoNotDiscover
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


@DoNotDiscover
class FileIOSpecJSON extends AnyWordSpec with Matchers {

  "The JSON FileIO" when {
    "making use of the FileIO Implementation" should {
      val fileIo = new FileIO

      val controller = new Controller
      var gameField = controller.gameFieldBuilder.getGameField

      "save and load a savefile correctly" in {
        gameField.moveTo(0, 1, 0, 2)
        val old = controller.gameFieldBuilder.getGameField
        fileIo.saveGame(controller.gameFieldBuilder)

        gameField.moveTo(1, 6, 1, 4)
        controller.gameFieldBuilder.getGameField.toString should not be old

        val (v, _) = fileIo.loadGame
        controller.updateGameField(v)
        controller.gameFieldBuilder.getGameField should be (old)
      }
    }
  }

}
