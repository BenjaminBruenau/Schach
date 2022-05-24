package Schach.controller.controllerComponent

import Schach.util.{Caretaker, Originator, UndoManager}
import model.gameManager.ChessGameFieldBuilderInterface
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.{GameFieldInterface, GameStatus}
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField

import java.awt.Color
import scala.swing.Publisher

trait ControllerInterface extends Publisher with Originator {
  val undoManager : UndoManager
  val caretaker : Caretaker

  def createGameField(): Vector[Figure]
  def controlInput(line: String): Boolean
  def gameFieldToString: String
  def getGameField: Vector[Figure]
  def movePiece(newPos: Vector[Int]): Boolean
  def refreshStatus() : Int
  def moveIsValid(newPos: Vector[Int]): Boolean
  def getGameStatus() : Int
  def setGameStatus(newState : GameStatus) : GameStatus
  def getPlayer() : Color
  def setPlayer(color : Color): Color
  def changePlayer(): Color
  def clear(): Boolean
  def convertPawn(figureType : String): Option[Figure]
  def updateGameField(newField : Vector[Figure]): Vector[Figure]
  def replaceGameField(gameField: GameField): GameField
  def isChecked(): Boolean
  def isCheckmate(): Boolean
  def undo(): Vector[Figure]
  def redo(): Vector[Figure]
  def save(): Unit
  def restore(): Unit
  def caretakerIsCalled(): Boolean
  def saveGame(): Vector[Figure]
  def loadGame(): Vector[Figure]
  def listSaves(): Vector[(Long, GameField)]
  def printGameStatus(): String
  def readInput(line: String): Vector[Int]
  def getPoint(input: Char): Int
}
