package Schach.model.gameFieldComponent.gameFieldBaseImpl

import java.awt.Color
import Schach.model.figureComponent.*
import Schach.model.gameFieldComponent.{ChessGameFieldBuilderInterface, GameStatus}
import Schach.util.GameFieldBuilder

import scala.collection.immutable.Vector

/** Responsible of initialising and managing a GameField
 *
 */
class ChessGameFieldBuilder extends GameFieldBuilder with ChessGameFieldBuilderInterface {

  private var instance : GameField = GameField(Vector(), GameStatus.Running, Color.WHITE)

  private def buildWhite(): Vector[Figure] = {
    instance.addFigures(Vector(
      Figure("Rook", 0, 0, Color.WHITE), Figure("Knight", 1, 0, Color.WHITE),
      Figure("Bishop", 2, 0, Color.WHITE), Figure("King", 4, 0, Color.WHITE),
      Figure("Queen", 3, 0, Color.WHITE), Figure("Bishop", 5, 0, Color.WHITE),
      Figure("Knight", 6, 0, Color.WHITE), Figure("Rook", 7, 0, Color.WHITE),
      Figure("Pawn", 0, 1, Color.WHITE), Figure("Pawn", 1, 1, Color.WHITE),
      Figure("Pawn", 2, 1, Color.WHITE), Figure("Pawn", 3, 1, Color.WHITE),
      Figure("Pawn", 4, 1, Color.WHITE), Figure("Pawn", 5, 1, Color.WHITE),
      Figure("Pawn", 6, 1, Color.WHITE), Figure("Pawn", 7, 1, Color.WHITE)))
  }

  private def buildBlack(): Vector[Figure] = {
    instance.addFigures(Vector(
      Pawn(0, 6, Color.BLACK), Pawn(1, 6, Color.BLACK),
      Pawn(2, 6, Color.BLACK), Pawn(3, 6, Color.BLACK),
      Pawn(4, 6, Color.BLACK), Pawn(5, 6, Color.BLACK),
      Pawn(6, 6, Color.BLACK), Pawn(7, 6, Color.BLACK),
      Rook(0, 7, Color.BLACK), Knight(1, 7, Color.BLACK),
      Bishop(2, 7, Color.BLACK), King(4, 7, Color.BLACK),
      Queen(3, 7, Color.BLACK), Bishop(5, 7, Color.BLACK),
      Knight(6, 7, Color.BLACK), Rook(7, 7, Color.BLACK)))
  }

  def makeGameField() : GameField = {
    updateGameField(Vector.empty, GameStatus.Running, Color.WHITE)
    updateGameField(buildBlack())
    updateGameField(buildWhite())
    instance
  }

  def updateGameField(newField: Vector[Figure] = instance.gameField,
                      newStatus: GameStatus = instance.status,
                      newPlayer: Color = instance.currentPlayer): GameField =
    instance = instance.copy(newField, newStatus, newPlayer)
    instance


  override def getGameField: GameField = {
    instance
  }

  override def getNewGameField: GameField = {
    makeGameField()
    instance
  }

}