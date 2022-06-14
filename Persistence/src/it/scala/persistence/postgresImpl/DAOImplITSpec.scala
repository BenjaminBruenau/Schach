package persistence.postgresImpl

import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService, ForAllTestContainer}
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.gameFieldComponent.GameStatus
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.testcontainers.containers.wait.strategy.Wait

import java.awt.Color
import java.io.File

class DAOImplITSpec extends AsyncWordSpec with Matchers with ForAllTestContainer {
  override val container: DockerComposeContainer = DockerComposeContainer(
    new File("Persistence/src/it/resources/docker-compose-test-postgres.yml"),
    exposedServices = Seq(
      ExposedService("postgres", 5432, Wait.forListeningPort())
    )
  )

  "A Postgres Implementation for DAO" when {

    "setting up the container" should {
      "wait for the DockerComposeContainer to be ready" in {
        assert(container.getServicePort("postgres", 5432) > 0)
      }
    }

    "making use of its API" should {

      "save a game in" in {
        val dao = new DAOImpl("jdbc:postgresql://localhost:5432/schachdb")

        val gameFieldBuilder = new ChessGameFieldBuilder
        val gameField = gameFieldBuilder.getNewGameField
        val saveGameFuture = dao.saveGame(gameField)

        saveGameFuture map {
          success => success should be(true)
        }
      }

      "load a game" in {
        val dao = new DAOImpl("jdbc:postgresql://localhost:5432/schachdb")

        val gameFieldBuilder = new ChessGameFieldBuilder
        val field = gameFieldBuilder.getNewGameField

        val loadGameFuture = dao.loadGame(0)

        loadGameFuture map {
          gameField => gameField shouldEqual field
        }
      }

      "list game saves" in {
        val dao = new DAOImpl("jdbc:postgresql://localhost:5432/schachdb")

        val getGameSavesFuture = dao.listSaves

        getGameSavesFuture map {
          saves =>
            saves shouldBe a[Vector[(Long, GameField)]]
            saves.head._1 should be (0)
        }
      }
    }
  }
}
