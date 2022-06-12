package Schach.controller.controllerComponent.controllerBaseImpl

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.api.HttpServiceInterface
import Schach.controller.controllerComponent.api.httpServiceMockImpl.HttpService
import Schach.util.Observer
import com.google.inject.{AbstractModule, Guice, Injector}
import model.gameModel.figureComponent.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import model.gameModel.gameFieldComponent.GameStatus

import java.awt.Color
import scala.swing.Reactor

class ControllerSpec extends AnyWordSpec with Matchers {

  var injector: Injector = Guice.createInjector(new MockModule)


  class MockModule extends AbstractModule {
    override def configure() : Unit = {
      bind(classOf[ControllerInterface]).toInstance(new Controller(new HttpService))
      bind(classOf[HttpServiceInterface]).to(classOf[HttpService])
    }
  }
  val controller : ControllerInterface = injector.getInstance(classOf[ControllerInterface])

  class ReactorListener extends Reactor {
    var gameFieldChanged: Boolean = false
    var statusChanged: Boolean = false
    var exceptionOccurred: Boolean = false

    listenTo(controller)

    reactions += {
      case _: GameFieldChanged =>
        gameFieldChanged = true
      case StatusChanged(_, _) => statusChanged = true
      case ExceptionOccurred(_) => exceptionOccurred = true
    }
  }

  val pawnReachedEndVector: Vector[Figure] = Vector(
    Figure("Rook", 0, 0, Color.WHITE), Figure("Knight", 1, 0, Color.WHITE),
    Figure("Bishop", 2, 0, Color.WHITE), Figure("King", 4, 0, Color.WHITE),
    Figure("Queen", 3, 0, Color.WHITE), Figure("Bishop", 5, 0, Color.WHITE),
    Figure("Knight", 6, 0, Color.WHITE), Figure("Rook", 7, 0, Color.WHITE),
    Figure("Pawn", 0, 1, Color.WHITE), Figure("Pawn", 1, 1, Color.WHITE),
    Figure("Pawn", 2, 1, Color.WHITE), Figure("Pawn", 3, 1, Color.WHITE),
    Figure("Pawn", 4, 1, Color.WHITE), Figure("Pawn", 5, 1, Color.WHITE),
    Figure("Pawn", 6, 1, Color.WHITE), Figure("Pawn", 7, 7, Color.WHITE),
    Pawn(0, 6, Color.BLACK), Pawn(1, 6, Color.BLACK),
    Pawn(2, 6, Color.BLACK), Pawn(3, 6, Color.BLACK),
    Pawn(4, 6, Color.BLACK), Pawn(5, 6, Color.BLACK),
    Pawn(6, 6, Color.BLACK), Pawn(7, 6, Color.BLACK),
    Rook(0, 7, Color.BLACK), Knight(1, 7, Color.BLACK),
    Bishop(2, 7, Color.BLACK), King(4, 7, Color.BLACK),
    Queen(3, 7, Color.BLACK), Bishop(5, 7, Color.BLACK),
    Knight(6, 7, Color.BLACK), Rook(7, 1, Color.BLACK))

  val checkVector: Vector[Figure] = Vector(King(4, 7, Color.WHITE), Pawn(5, 6, Color.BLACK))
  val checkmateVector: Vector[Figure] = Vector(King(7, 7, Color.WHITE), Queen(6, 6, Color.BLACK), Rook(7, 6, Color.BLACK))

  "A Controller" when  {
    "observed by an Reactor" should {
      val vec = Vector(0, 1, 0, 2)

      "notify its reactor after init" in {
        val reactor = new ReactorListener

        controller.createGameField()
        Thread.sleep(50)

        reactor.gameFieldChanged should be(true)
      }

      "notify its reactor after moving a piece" in {
        val reactor = new ReactorListener
        
        controller.movePiece(vec)
        controller.changePlayer()
        
        reactor.gameFieldChanged should be(true)
      }

      "publish its status after an invalid move" in {
        val reactor = new ReactorListener

        controller.movePiece(Vector(1, 1 , 1, 8))

        reactor.statusChanged should be(true)
      }

      "check if a move is valid" in {
        val reactor = new ReactorListener
        val v2 = Vector(7, 7, 1, 1)

        for {
          _ <- controller.createGameField()
        } yield reactor.gameFieldChanged should be(true)

        controller.moveIsValid(vec) should be(true)
        controller.moveIsValid(v2) should be(false)
        controller.isCheckmate() should be(false)
        reactor.statusChanged should be(true)
      }

      "convert a Pawn into a Queen" in {
        controller.gameField = controller.gameField.copy(gameField = pawnReachedEndVector)

        controller.convertPawn("queen").get shouldBe a[Queen]
      }

      "convert a Pawn into a Rook" in {
        controller.gameField = controller.gameField.copy(gameField = pawnReachedEndVector)

        controller.convertPawn("rook").get shouldBe a[Rook]
      }

      "convert a Pawn into a Knight" in {
        controller.gameField = controller.gameField.copy(gameField = pawnReachedEndVector)

        controller.convertPawn("knight").get shouldBe a[Knight]
      }

      "convert a Pawn into a Bishop" in {
        controller.gameField = controller.gameField.copy(gameField = pawnReachedEndVector)

        controller.convertPawn("bishop").get shouldBe a[Bishop]
      }

      "prevent converting a Pawn if no Pawn reached the end" in {
        controller.convertPawn("queen") should be(None)
      }

      "prevent converting a Pawn if the given piece type is invalid" in {
        controller.gameField = controller.gameField.copy(gameField = pawnReachedEndVector)

        controller.convertPawn("abc") should be(None)
      }

      "change the GameStatus accordingly" in {
        controller.gameField = controller.gameField.copy(gameField = pawnReachedEndVector)
        val reactor = new ReactorListener

        controller.refreshStatus()
        Thread.sleep(50)

        reactor.statusChanged should be(true)
      }

      "detect checkmate" in {
        controller.gameField = controller.gameField.copy(gameField = checkmateVector)
        val reactor = new ReactorListener

        controller.refreshStatus()
        Thread.sleep(50)

        reactor.statusChanged should be(true)
      }

      "return a string representation of the GameField" in {
        controller.gameFieldToString shouldBe a[String]
      }

    }

    "used as an Originator" should {
      val controller = injector.getInstance(classOf[ControllerInterface])

      "handle undo/redo correctly" in {
        controller.createGameField()
        val reactor = new ReactorListener

        controller.undo()
        reactor.gameFieldChanged should be(true)
        reactor.gameFieldChanged = false

        controller.redo()
        reactor.gameFieldChanged should be(true)
      }

      "save and restore a state" in {
        controller.createGameField()
        val reactor = new ReactorListener

        controller.save()
        for {
          _ <- controller.restore()
        } yield reactor.gameFieldChanged should be(true)
      }

      "inform if there is a current save state" in {
        controller.save()
        controller.caretakerIsCalled() should be(true)
      }

      "return a gameField via getGameField" in {
        controller.getGameField shouldBe a [Vector[_]]
      }
    }

    "used to save load and list game saves" should {

      "load a game save" in {
        val reactor = new ReactorListener

        for {
          _ <- controller.createGameField()
          _ <- controller.saveGame()
          _ <- controller.loadGame()
        } yield reactor.gameFieldChanged should be(true)
      }

      "list game saves" in {
        controller.saveGame()

        val saves =
        for {
          save <- controller.listSaves()
        } yield save

        saves.head._1 should be(1)
        saves.head._2.gameField shouldBe a[Vector[Figure]]
      }
    }
  }

}
