package persistence.fileIOJSONImpl

import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers

class DAOImplSpec extends AsyncWordSpec with Matchers {

  "A FileIO Implementation for DAO" should {
    val dao = new DAOImpl
    val gameFieldBuilder = new ChessGameFieldBuilder

    "save a game" in {
        val gameField = gameFieldBuilder.getNewGameField
        val saveGameFuture = dao.saveGame(gameField)

        saveGameFuture map {
          success => success should be(true)
        }
    }

    "load a game" in {
      val loadGameFuture = dao.loadGame(1)

      loadGameFuture map {
        gameField => gameField shouldBe a[GameField]
      }
    }

    "list game saves" in {
      val getGameSavesFuture = dao.listSaves

      getGameSavesFuture map {
        saves =>
          saves shouldBe a[Vector[(Long, GameField)]]
          saves.head._1 should be (1)
      }
    }
  }
}
