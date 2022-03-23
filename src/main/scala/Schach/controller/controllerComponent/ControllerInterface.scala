package Schach.controller.controllerComponent

import Schach.model.figureComponent.Figure
import Schach.model.gameFieldComponent.GameFieldInterface
import Schach.util.{Caretaker, Originator, UndoManager}

import java.awt.Color
import scala.swing.Publisher

trait ControllerInterface extends Publisher with Originator{
  val undoManager : UndoManager
  val caretaker : Caretaker
  var gameField : GameFieldInterface

  def createGameField(): Unit
  def controlInput(line: String): Boolean
  def gameFieldToString: String
  def getGameField: Vector[Figure]
  def movePiece(newPos: Vector[Int]): Boolean
  def getGameStatus() : Int
  def checkStatus() : Unit
  def moveIsValid(newPos: Vector[Int]): Boolean
  def setPlayer(color : Color): Color
  def getPlayer() : Color
  def changePlayer(): Unit
  def convertPawn(figureType : String): Unit
  def isChecked(): Boolean
  def isCheckmate(): Boolean
  def undo(): Unit
  def redo(): Unit
  def save(): Unit
  def restore(): Unit
  def caretakerIsCalled(): Boolean
  def saveGame(): Unit
  def loadGame(): Unit
  def printGameStatus(): String
  def readInput(line: String): Vector[Int]
}
