package Schach.controller.controllerComponent.controllerBaseImpl

import java.awt.Color
import Schach.GameFieldModule
import Schach.controller.controllerComponent._
import Schach.model.figureComponent.{Bishop, Figure, Knight, Queen, Rook}
import Schach.model.fileIOComponent.FileIOInterface
import Schach.model.gameFieldComponent.GameFieldInterface
import Schach.util.{Caretaker, UndoManager}
import com.google.inject.name.Names
import com.google.inject.{Guice, Inject, Injector}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.util.{Failure, Success, Try}

class Controller @Inject() extends ControllerInterface {
  var injector: Injector = Guice.createInjector(new GameFieldModule)
  val undoManager = new UndoManager
  val caretaker = new Caretaker
  var gameField: GameFieldInterface = injector.getInstance(classOf[GameFieldInterface])
  val fileIo: FileIOInterface = injector.getInstance(classOf[FileIOInterface])


  def createGameField(): Unit = {
    injector = Guice.createInjector(new GameFieldModule)
    gameField = injector.getInstance(classOf[GameFieldInterface])
    publish(new GameFieldChanged)
  }

  def controlInput(line: String): Boolean = {
    line.matches("[A-H][1-8]")
  }

  def gameFieldToString: String = gameField.toString

  def getGameField: Vector[Figure] = {
    gameField.getFigures
  }

  def movePiece(newPos: Vector[Int]): Boolean = {
    if (moveIsValid(newPos)) {
      undoManager.doStep(new MoveCommand(newPos(0), newPos(1), newPos(2), newPos(3), this))
      changePlayer()
      checkStatus()

      publish(new GameFieldChanged)
      publish( new StatusChanged(getGameStatus(), { if (getPlayer().getRed == 0) "BLACK" else "WHITE" }) )
      return true
    }
    publish( new StatusChanged(getGameStatus(), { if (getPlayer().getRed == 0) "BLACK" else "WHITE" }) )
    false
  }

  def checkStatus(): Unit = {
    if (isChecked()) {
      gameField.setStatus(gameField.CHECKED)
      if (isCheckmate())
        gameField.setStatus(gameField.CHECKMATE)
    }

    if (gameField.pawnHasReachedEnd())
      gameField.setStatus(gameField.PAWN_REACHED_END)
  }

  def moveIsValid(newPos: Vector[Int]): Boolean = {
    val valid = gameField.moveValid(newPos(0), newPos(1), newPos(2), newPos(3))

    if (valid) gameField.setStatus(gameField.RUNNING)
    else gameField.setStatus(gameField.MOVE_ILLEGAL)

    valid
  }

  def getGameStatus() : Int = {
    gameField.getStatus()
  }

  def setPlayer(color: Color): Color = {
    gameField.setPlayer(color)
  }

  def getPlayer(): Color = {
    gameField.getPlayer
  }

  def changePlayer(): Unit = {
    gameField.changePlayer()
  }

  def convertPawn(figureType : String): Unit = {

    Try(gameField.getPawnAtEnd()) match {
      case Success(pawn) => {
        figureType match {
          case "queen" => gameField.convertFigure(pawn, Queen(pawn.x, pawn.y, pawn.color))
          case "rook" => gameField.convertFigure(pawn, Rook(pawn.x, pawn.y, pawn.color))
          case "knight" => gameField.convertFigure(pawn, Knight(pawn.x, pawn.y, pawn.color))
          case "bishop" => gameField.convertFigure(pawn, Bishop(pawn.x, pawn.y, pawn.color))
          case _=> return
        }
        publish(new GameFieldChanged)
      }
      case Failure(_) => println("No Pawn Reached the End!")
    }

  }

  def isChecked(): Boolean = {
    gameField.isChecked(getPlayer())
  }

  def isCheckmate(): Boolean = {
    gameField.isCheckmate(getPlayer())
  }

  def undo(): Unit = {
    undoManager.undoStep()
    publish(new GameFieldChanged)
  }

  def redo(): Unit = {
    undoManager.redoStep()
    publish(new GameFieldChanged)
  }

  def save(): Unit = {
    val memento = new GameFieldMemento(gameField.getFigures, gameField.getPlayer)
    caretaker.called = true
    caretaker.addMemento(memento)
  }

  def restore(): Unit = {
    gameField.clear()
    gameField.addFigures(caretaker.getMemento.getFigures)
    publish(new GameFieldChanged)
  }

  def caretakerIsCalled(): Boolean = {
    caretaker.called
  }

  def saveGame(): Unit = {
    fileIo.saveGame(gameField)
  }

  def loadGame(): Unit = {
    gameField.clear()
    val (vec, col) = fileIo.loadGame
    gameField.addFigures(vec)
    gameField.setPlayer(col)
    publish(new GameFieldChanged)
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

  private def getPoint(input: Char): Int = {
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
