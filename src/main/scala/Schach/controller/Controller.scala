package Schach.controller

import Schach.model.{ChessGameFieldBuilder, GameField}
import Schach.util.Observable

class Controller(var gameField: GameField) extends Observable{


  def createGameField() : Unit = {
    val builder = new ChessGameFieldBuilder
    builder.makeGameField()
    gameField = builder.getGameField
    notifyObservers
  }

  def controlInput(line: String): Boolean = {
    line.matches("[A-H][1-8]")
  }

  def gameFieldToString: String = gameField.toString

  def movePiece(newPos: Vector[Int]): Unit = {
    gameField = gameField.moveTo(newPos(0), newPos(1), newPos(2), newPos(3))
    notifyObservers
  }

  def moveIsValid(newPos: Vector[Int]): Boolean = {
    gameField.moveValid(newPos(0), newPos(1), newPos(2), newPos(3))
  }


}