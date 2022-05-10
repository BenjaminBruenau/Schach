package fileIOComponent.fileIOJSONImpl

import org.scalatest.DoNotDiscover
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._


@DoNotDiscover
class FileIOSpecJSON extends AnyWordSpec with Matchers with GameFieldJsonProtocol with SprayJsonSupport {

  "The JSON FileIO" when {
    "making use of the FileIO Implementation" should {
      val fileIo = new FileIO

      val builder = new ChessGameFieldBuilder

      "save and load a savefile correctly" in {
        builder.updateGameField(builder.getGameField.moveTo(0, 1, 0, 2))
        val old = builder.getGameField
        fileIo.saveGame(builder.getGameField.toJson)

        builder.updateGameField(builder.getGameField.moveTo(1, 6, 1, 4))
        builder.getGameField.toString should not be old

        val field = fileIo.loadGame
        builder.updateGameField(field)
        builder.getGameField should be (old)
      }
    }
  }

}
