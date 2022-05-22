package model.gameManager

import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.GameStatus
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField

import java.awt.Color

trait ChessGameFieldBuilderInterface {

  /**
   * Sets up a normal chess field
   */
  def makeGameField() : GameField

  /**
   * Updates a chess field
   */
  def updateGameField(newField: Vector[Figure] = Vector.empty, newStatus: GameStatus = GameStatus.Running, newPlayer: Color = Color.WHITE): GameField

  /**
   *
   * @return actual gameField
   */
  def getGameField: GameField

  /**
   *  Sets up a new chess field.
   *
   * @return new gameField
   */
  def getNewGameField: GameField
}