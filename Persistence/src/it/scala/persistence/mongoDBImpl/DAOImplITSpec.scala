package persistence.mongoDBImpl

import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService, ForAllTestContainer}
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import model.gameModel.gameFieldComponent.GameStatus
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.testcontainers.containers.wait.strategy.Wait

import java.awt.Color
import java.io.File
import java.time.Duration
import java.time.temporal.ChronoUnit

class DAOImplITSpec extends AsyncWordSpec with Matchers with ForAllTestContainer {
  override val container: DockerComposeContainer = DockerComposeContainer(
    new File("Persistence/src/it/resources/docker-compose-test-mongo.yml"),
    exposedServices = Seq(
      ExposedService("mongodb", 27017, Wait.forListeningPort())
    )
  )

  "A MongoDB Implementation for DAO" when {

    "setting up the container" should {
      "wait for the DockerComposeContainer to be ready" in {
        assert(container.getServicePort("mongodb", 27017) > 0)
      }
    }

    "making use of its API" should {
      val dao = new DAOImpl("mongodb://localhost:27017")

      "save a game in" in {
        val gameFieldBuilder = new ChessGameFieldBuilder
        val gameField = gameFieldBuilder.getNewGameField
        val saveGameFuture = dao.saveGame(gameField)

        saveGameFuture map {
          success => success should be(true)
        }
      }

      "load a game" in {
        val loadGameFuture = dao.loadGame(1)

        loadGameFuture map {
          gameField => gameField shouldEqual gameField
        }
      }

      "load a game without cache" in {
        dao.cachedSaves = Vector.empty
        val gameFieldBuilder = new ChessGameFieldBuilder
        val field = gameFieldBuilder.getNewGameField
        
        val loadGameFuture = dao.loadGame(1)
        

        loadGameFuture map {
          gameField => gameField shouldEqual field
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

      "verify cache replacement" in {
        val gameFieldBuilder = new ChessGameFieldBuilder
        val gameField = gameFieldBuilder.getNewGameField

        dao.cachedSaves = Vector.empty
        dao.cachedSaves = Vector.tabulate(50){
          n => (n, GameField(Vector.empty, GameStatus.Running, Color.WHITE))
        }

        val saveGameFuture = dao.saveGame(gameField)

        saveGameFuture map {
          success =>
            success should be(true)
            dao.cachedSaves.last._2.toString should be(gameField.toString)
        }
      }

    }
  }
}
