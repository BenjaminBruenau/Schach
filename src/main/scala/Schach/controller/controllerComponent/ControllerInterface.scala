package Schach.controller.controllerComponent

import Schach.model.figureComponent.Figure
import Schach.model.gameFieldComponent.{GameFieldInterface, GameStatus}
import Schach.util.{Caretaker, Originator, UndoManager}

import java.awt.Color
import scala.swing.Publisher

trait ControllerInterface extends Publisher with Originator {
  val undoManager : UndoManager
  val caretaker : Caretaker
  var gameField : GameFieldInterface

  def createGameField(): Vector[Figure]
  def controlInput(line: String): Boolean
  def gameFieldToString: String
  def getGameField: Vector[Figure]
  def movePiece(newPos: Vector[Int]): Boolean
  def getGameStatus() : Int
  def checkStatus() : GameStatus
  def moveIsValid(newPos: Vector[Int]): Boolean
  def setPlayer(color : Color): Color
  def getPlayer() : Color
  def changePlayer(): Color
  def convertPawn(figureType : String): Option[Vector[Figure]]
  def isChecked(): Boolean
  def isCheckmate(): Boolean
  def undo(): Vector[Figure]
  def redo(): Vector[Figure]
  def save(): Unit
  def restore(): Unit
  def caretakerIsCalled(): Boolean
  def saveGame(): Vector[Figure]
  def loadGame(): Vector[Figure]
  def printGameStatus(): String
  def readInput(line: String): Vector[Int]
  def getPoint(input: Char): Int
}
