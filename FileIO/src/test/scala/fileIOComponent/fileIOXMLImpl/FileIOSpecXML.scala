package fileIOComponent.fileIOXMLImpl

import fileIOComponent.fileIOJSONImpl.FileIO
import gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class FileIOSpecXML extends AnyWordSpec with Matchers {

  "The XML FileIO" when {
    "making use of the FileIO Implementation" should {
      val fileIo = new FileIO

      val builder = new ChessGameFieldBuilder

      "save and load a savefile correctly" in {
        builder.updateGameField(builder.getGameField.moveTo(0, 1, 0, 2))
        val old = builder.getGameField
        fileIo.saveGame(builder)

        builder.updateGameField(builder.getGameField.moveTo(1, 6, 1, 4))
        builder.getGameField.toString should not be old

        val (v, _) = fileIo.loadGame
        builder.updateGameField(v)
        builder.getGameField should be (old)
      }
    }
  }

}
