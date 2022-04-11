package Schach.model.gameFieldComponent.gameFieldBaseImpl

import Schach.model.figureComponent.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.awt.Color
import Schach.model.gameFieldComponent.{GameFieldInterface, GameStatus}
//ToDo: Fix Tests by checking returned Vector since instance isnt changing anymore
class GameFieldSpec extends AnyWordSpec with Matchers {

  "A GameField" should {
    val builder = new ChessGameFieldBuilder
    var gameField : GameFieldInterface = builder.getNewGameField
    val figure = gameField.getFigure(1, 1)

    "have some states" in {
      figure.get shouldBe a[Pawn]
      GameStatus.Running.value should be (0)
      GameStatus.Checked.value should be (1)
      GameStatus.Checkmate.value should be (2)
      GameStatus.MoveIllegal.value should be (3)
      GameStatus.PawnReachedEnd.value should be (4)
    }

    "make moves" in {
      gameField.moveTo(1, 1, 2, 3)
      gameField.getFigure(2,3).get shouldBe a[Pawn]
    }

    "cover some specific moves and check if the way is free" in {
      gameField = builder.getNewGameField
      gameField.wayToIsFreeDiagonal(2,0,6,4) should be(false)
      gameField.wayToIsFreeDiagonal(3,2,0,5) should be(true)
      gameField.wayToIsFreeDiagonal(0, 3, 1, 2) should be(true)
      gameField.wayToIsFreeDiagonal(0, 5, 3, 2) should be(true)

      gameField.moveTo(3,1,3,3)
      gameField.moveTo(7,6,7,5)
      gameField.moveTo(4,0,0,4)

      gameField.moveValid(2, 6, 2, 5) should be (false)
      gameField.wayToIsFreeStraight(0,4,2,4) should be(true)
    }

    "cover some more move cases" in {
      builder.getNewGameField
      gameField.moveTo(0, 1, 0, 3)
      gameField.moveTo(0, 0, 0, 2)
      gameField.getFigure(0,2).get shouldBe a[Rook]

      gameField.moveTo(1, 0, 2, 2)
      gameField.getFigure(2, 2).get shouldBe a[Knight]
      gameField.moveTo(1, 5, 2, 5)
      gameField.getFigure(2, 5) should be(None)

      gameField = builder.getNewGameField
      gameField.moveTo(2, 0, 2, 1)
      gameField.getFigure(2, 1).get shouldBe a[Bishop]

    }

    "cover some cases for black pieces trying to move" in {
      gameField = builder.getNewGameField
      gameField.wayToIsFreeStraight(1, 6, 1, 4) should be(true)

      gameField.moveTo(1, 6, 1, 4 )
      gameField.wayToIsFreeDiagonal(2, 7, 0, 5) should be(true)

      gameField.moveTo(7, 7, 7, 5)
      gameField.wayToIsFreeStraight(7, 5, 4, 5) should be(true)
    }

    "check if moving to a specific cell is allowed" in {
      gameField = builder.getNewGameField
      gameField.moveToFieldAllowed(1, 0, gameField.getFigure(1,1).get) should be(false)
      gameField.moveToFieldAllowed(0, 2, gameField.getFigure(1,1).get) should be(true)
      gameField.moveToFieldAllowed(2, 1, gameField.getFigure(1,1).get) should be (false)
    }

    "have a nice String representation" in {
      gameField.toString shouldBe a[String]
    }

    "make use of Figure's equals method" in {
      gameField = builder.getNewGameField
      val f3 = gameField.getFigure(0,1).get
      val f4 = gameField.getFigure(0,3)
      val f = new Figure {
        override val x: Int = 1
        override val y: Int = 2
        override val name: String = "test"
        override val color: Color = Color.WHITE
      }
      f.equals(f3) should be (false)
      f.equals(f4) should be (false)
      gameField.getFigure(2,7).get shouldBe a[Bishop]
    }

    "check for checkmate" in {
      gameField = builder.getNewGameField
      val p = gameField.getFigure(0, 1).get
      val king = gameField.getFigure(4, 7).get

      gameField.setSelfIntoCheck(p, 0, 2) should be (false)
      gameField.moveTo(5, 1, 5, 2)
      gameField.moveTo(3, 6, 3, 4)
      gameField.moveTo(6, 0, 2, 4)
      gameField.setSelfIntoCheck(king, 3, 6) should be(true)

      gameField = builder.getNewGameField
      gameField.moveTo(6, 0, 5, 2)
      gameField.moveTo(0, 6, 0, 5)
      gameField.moveTo(5, 2, 6, 4)
      gameField.moveTo(0, 5, 0, 4)
      gameField.moveTo(6, 4, 5, 6)
      gameField.isCheckmate(Color.BLACK) should be(false)
    }

    "add Figures correctly" in {
      gameField = builder.getNewGameField
      val old = gameField.toString

      val vec = Vector(Rook(0, 0, Color.WHITE), Knight(1, 0, Color.WHITE), Bishop(2, 0, Color.WHITE), King(3, 0, Color.WHITE),
        Queen(4, 0, Color.WHITE), Bishop(5, 0, Color.WHITE), Knight(6, 0, Color.WHITE), Rook(7, 0, Color.WHITE),
        Pawn(0, 1, Color.WHITE), Pawn(1, 1, Color.WHITE), Pawn(2, 1, Color.WHITE), Pawn(3, 1, Color.WHITE),
        Pawn(4, 1, Color.WHITE), Pawn(5, 1, Color.WHITE), Pawn(6, 1, Color.WHITE), Pawn(7, 1, Color.WHITE))

      gameField.addFigures(vec)
      gameField.toString should be(old)
    }
  }
}