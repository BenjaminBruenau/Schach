package Schach.controller.controllerComponent.controllerBaseImpl

import java.awt.Color
import Schach.GameFieldModule
import Schach.controller.controllerComponent.*
import Schach.model.figureComponent.{Bishop, Figure, Knight, Queen, Rook}
import Schach.model.fileIOComponent.FileIOInterface
import Schach.model.gameFieldComponent.{ChessGameFieldBuilderInterface, GameFieldInterface, GameStatus}
import Schach.util.{Caretaker, UndoManager}
import com.google.inject.name.Names
import com.google.inject.{Guice, Inject, Injector}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.collection.immutable.Vector
import scala.util.{Failure, Success, Try}

class Controller @Inject() extends ControllerInterface {
  var injector: Injector = Guice.createInjector(new GameFieldModule)
  val undoManager = new UndoManager
  val caretaker = new Caretaker
  var gameFieldBuilder: ChessGameFieldBuilderInterface = injector.getInstance(classOf[ChessGameFieldBuilderInterface])
  val fileIo: FileIOInterface = injector.getInstance(classOf[FileIOInterface])


  def createGameField(): Vector[Figure] = {
    injector = Guice.createInjector(new GameFieldModule)
    gameFieldBuilder = injector.getInstance(classOf[ChessGameFieldBuilderInterface])
    gameFieldBuilder.makeGameField()
    publish(new GameFieldChanged)
    getGameField
  }

  def controlInput(line: String): Boolean = line.matches("[A-H][1-8]")

  def gameFieldToString: String = gameFieldBuilder.getGameField.toString

  def getGameField: Vector[Figure] = gameFieldBuilder.getGameField.getFigures

  def movePiece(newPos: Vector[Int]): Boolean = {
    if (moveIsValid(newPos)) {
      undoManager.doStep(new MoveCommand(newPos(0), newPos(1), newPos(2), newPos(3), this))
      changePlayer()
      refreshStatus()

      publish( GameFieldChanged() )
      publish( StatusChanged(getGameStatus(), { if (getPlayer().getRed == 0) "BLACK" else "WHITE" }) )
      return true
    }
    publish( StatusChanged(getGameStatus(), { if (getPlayer().getRed == 0) "BLACK" else "WHITE" }) )
    false
  }

  def refreshStatus(): Int = {
    if (isChecked()) {
      setGameStatus(GameStatus.Checked)
      if (isCheckmate())
        setGameStatus(GameStatus.Checkmate)
    }

    if (gameFieldBuilder.getGameField.pawnHasReachedEnd())
      setGameStatus(GameStatus.PawnReachedEnd)

    getGameStatus()
  }

  def moveIsValid(newPos: Vector[Int]): Boolean = {
    val valid = gameFieldBuilder.getGameField.moveValid(newPos(0), newPos(1), newPos(2), newPos(3))

    if (valid) setGameStatus(GameStatus.Running)
    else setGameStatus(GameStatus.MoveIllegal)

    valid
  }

  def getGameStatus() : Int = gameFieldBuilder.getGameField.status.value

  def setGameStatus(newState : GameStatus): GameStatus = {
    gameFieldBuilder.updateGameField(newStatus = newState)
    gameFieldBuilder.getGameField.status
  }

  def getPlayer(): Color = gameFieldBuilder.getGameField.currentPlayer

  def setPlayer(color: Color): Color = {
    gameFieldBuilder.updateGameField(newPlayer = color)
    gameFieldBuilder.getGameField.currentPlayer
  }

  def changePlayer(): Color = {
    gameFieldBuilder.getGameField.currentPlayer match {
      case Color.BLACK => setPlayer(Color.WHITE)
      case Color.WHITE => setPlayer(Color.BLACK)
    }
  }

  def clear() : Boolean = {
    gameFieldBuilder.updateGameField(Vector.empty, GameStatus.Running, newPlayer = Color.WHITE)
    gameFieldBuilder.getGameField.gameField.isEmpty
  }

  def convertPawn(figureType : String): Option[Figure] = {

    Try(gameFieldBuilder.getGameField.getPawnAtEnd()) match {
      case Success(pawn) => {
        val (newField, convertedPiece) = figureType match {
          case "queen" =>
            (gameFieldBuilder.getGameField.convertFigure(pawn, Queen(pawn.x, pawn.y, pawn.color)),
            Queen(pawn.x, pawn.y, pawn.color))
          case "rook" =>
            (gameFieldBuilder.getGameField.convertFigure(pawn, Rook(pawn.x, pawn.y, pawn.color)),
            Rook(pawn.x, pawn.y, pawn.color))
          case "knight" =>
            (gameFieldBuilder.getGameField.convertFigure(pawn, Knight(pawn.x, pawn.y, pawn.color)),
            Knight(pawn.x, pawn.y, pawn.color))
          case "bishop" =>
            (gameFieldBuilder.getGameField.convertFigure(pawn, Bishop(pawn.x, pawn.y, pawn.color)),
            Bishop(pawn.x, pawn.y, pawn.color))
          case _=> return None
        }
        gameFieldBuilder.updateGameField(newField = newField)
        publish(new GameFieldChanged)
        Some(convertedPiece)
      }
      case Failure(_) => println("No Pawn Reached the End!")
      None
    }

  }

  def updateGameField(newField : Vector[Figure]): Vector[Figure] =
    gameFieldBuilder.updateGameField(newField = newField)
    getGameField

  def isChecked(): Boolean = gameFieldBuilder.getGameField.isChecked(getPlayer())

  def isCheckmate(): Boolean = gameFieldBuilder.getGameField.isCheckmate(getPlayer())

  def undo(): Vector[Figure] = {
    undoManager.undoStep()
    publish(new GameFieldChanged)
    getGameField
  }

  def redo(): Vector[Figure] = {
    undoManager.redoStep()
    publish(new GameFieldChanged)
    getGameField
  }

  def save(): Unit = {
    val memento = new GameFieldMemento(getGameField, getPlayer())
    caretaker.called = true
    caretaker.addMemento(memento)
  }

  def restore(): Unit = {
    clear()
    gameFieldBuilder.updateGameField(newField = caretaker.getMemento.getFigures)
    publish(new GameFieldChanged)
  }

  def caretakerIsCalled(): Boolean = caretaker.called

  def saveGame(): Vector[Figure] = fileIo.saveGame(gameFieldBuilder)

  def loadGame(): Vector[Figure] = {
    clear()
    val (vec, col) = fileIo.loadGame
    gameFieldBuilder.updateGameField(newField = caretaker.getMemento.getFigures, newPlayer = col)
    publish(new GameFieldChanged)
    getGameField
  }

  def printGameStatus(): String = {
    getGameStatus() match {
      case 0 => "PLAYER " + { if (getPlayer().getRed == 0) "BLACK"
      else "WHITE"} + "`S Turn"
      case 1 => "PLAYER " + { if (getPlayer().getRed == 0) "BLACK"
      else "WHITE"} + "IS CHECKED"
      case 2 => {if (getPlayer().getRed == 0) "BLACK "
      else "WHITE "} + "IS CHECKMATE"
      case 3 => "INVALID MOVE"
      case 4 => ""
      //println("PAWN HAS REACHED THE END")
    }
  }

  def readInput(line: String): Vector[Int] = {
    val fromX = getPoint(line.charAt(0))
    val fromY = getPoint(line.charAt(1))
    val toX = getPoint(line.charAt(3))
    val toY = getPoint(line.charAt(4))
    Vector(fromX, fromY, toX, toY)
  }

  def getPoint(input: Char): Int = {
    input match {
      case 'A' => 0
      case 'B' => 1
      case 'C' => 2
      case 'D' => 3
      case 'E' => 4
      case 'F' => 5
      case 'G' => 6
      case 'H' => 7
      case '1' => 0
      case '2' => 1
      case '3' => 2
      case '4' => 3
      case '5' => 4
      case '6' => 5
      case '7' => 6
      case '8' => 7
      case _ => -1
    }
  }


}
