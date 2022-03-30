package Schach.util

import Schach.model.gameFieldComponent.gameFieldBaseImpl.GameField

trait GameFieldBuilder {

  def makeGameField() : GameField

  def getGameField : GameField

  def getNewGameField : GameField

  //def setGameField(newField : GameField) : GameField
}
