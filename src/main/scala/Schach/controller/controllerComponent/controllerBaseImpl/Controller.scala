package Schach.controller.controllerComponent.controllerBaseImpl

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.api.ControllerRestController
import Schach.util.{Caretaker, UndoManager}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.google.inject.name.Names
import com.google.inject.{Guice, Inject, Injector}
import com.typesafe.config.{Config, ConfigFactory}
import model.gameModel.figureComponent.*
import model.gameManager.ChessGameFieldBuilderInterface
import model.gameModel.figureComponent
import model.gameModel.gameFieldComponent.{GameFieldInterface, GameFieldJsonProtocol, GameStatus}
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import spray.json.*

import java.awt.Color
import scala.collection.immutable.Vector
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

class Controller @Inject() extends ControllerInterface {
  var injector: Injector = Guice.createInjector(new GameFieldModule)
  val undoManager = new UndoManager
  val caretaker = new Caretaker
  val httpService = new HttpService



  def createGameField(): Vector[figureComponent.Figure] = {
    injector = Guice.createInjector(new GameFieldModule)
    httpService.makeGameField
    publish(new GameFieldChanged)
    getGameField
  }

  def controlInput(line: String): Boolean = line.matches("[A-H][1-8]")

  def gameFieldToString: String =
    val game = httpService.getGameField
    game.toString

  def getGameField: Vector[figureComponent.Figure] = httpService.getGameField.getFigures

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

    if (httpService.getGameField.pawnHasReachedEnd())
      setGameStatus(GameStatus.PawnReachedEnd)

    getGameStatus()
  }

  def moveIsValid(newPos: Vector[Int]): Boolean = {
    val valid = httpService.getGameField.moveValid(newPos(0), newPos(1), newPos(2), newPos(3))

    if (valid) setGameStatus(GameStatus.Running)
    else setGameStatus(GameStatus.MoveIllegal)

    valid
  }

  def getGameStatus() : Int = httpService.getGameField.status.value

  def setGameStatus(newState : GameStatus): GameStatus = httpService.updateStatus(newStatus = newState).status

  def getPlayer(): Color = httpService.getGameField.currentPlayer

  def setPlayer(color: Color): Color = httpService.updatePlayer(newPlayer = color).currentPlayer

  def changePlayer(): Color = {
    httpService.getGameField.currentPlayer match {
      case Color.BLACK => setPlayer(Color.WHITE)
      case Color.WHITE => setPlayer(Color.BLACK)
    }
  }

  def clear() : Boolean = httpService.updateGameField((Vector.empty, GameStatus.Running, Color.WHITE)).gameField.isEmpty

  def convertPawn(figureType : String): Option[figureComponent.Figure] = {
    val gameField = httpService.getGameField
    Try(gameField.getPawnAtEnd()) match {
      case Success(pawn) => {
        val (newField, convertedPiece) = figureType match {
          case "queen" =>
            (gameField.convertFigure(pawn, figureComponent.Queen(pawn.x, pawn.y, pawn.color)),
            figureComponent.Queen(pawn.x, pawn.y, pawn.color))
          case "rook" =>
            (gameField.convertFigure(pawn, figureComponent.Rook(pawn.x, pawn.y, pawn.color)),
            figureComponent.Rook(pawn.x, pawn.y, pawn.color))
          case "knight" =>
            (gameField.convertFigure(pawn, figureComponent.Knight(pawn.x, pawn.y, pawn.color)),
            figureComponent.Knight(pawn.x, pawn.y, pawn.color))
          case "bishop" =>
            (gameField.convertFigure(pawn, figureComponent.Bishop(pawn.x, pawn.y, pawn.color)),
            figureComponent.Bishop(pawn.x, pawn.y, pawn.color))
          case _=> return None
        }
        httpService.updateField(newField = newField)
        publish(new GameFieldChanged)
        Some(convertedPiece)
      }
      case Failure(_) => println("No Pawn Reached the End!")
      None
    }

  }

  def updateGameField(newField : Vector[figureComponent.Figure]): Vector[figureComponent.Figure] =
    httpService.updateField(newField).gameField

  def isChecked(): Boolean = httpService.getGameField.isChecked(getPlayer())

  def isCheckmate(): Boolean = httpService.getGameField.isCheckmate(getPlayer())

  def undo(): Vector[figureComponent.Figure] = {
    undoManager.undoStep()
    publish(new GameFieldChanged)
    getGameField
  }

  def redo(): Vector[figureComponent.Figure] = {
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
    httpService.updateField(newField = caretaker.getMemento.getFigures)
    publish(new GameFieldChanged)
  }

  def caretakerIsCalled(): Boolean = caretaker.called

  def saveGame(): Vector[figureComponent.Figure] =
    httpService.saveGameViaHttp(httpService.getGameField)
    getGameField

  def loadGame(): Vector[figureComponent.Figure] = {
    clear()
    replaceGameField(httpService.loadGameViaHttp(1.toLong))
    publish(new GameFieldChanged)
    getGameField
  }

  def listSaves(): Vector[(Long, GameField)] = httpService.getGameSavesViaHttp

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

  /*
  .onComplete {
        case Success(gameField) =>
          gameFieldBuilder.updateGameField(
            newField = gameField.gameField, newPlayer = gameField.currentPlayer, newStatus = gameField.status)
        case Failure(exception) => println("Error while loading saved Game")
      }
  */


  
  def replaceGameField(gameField: GameField): GameField = 
    val newField = httpService.updateGameField(gameField.gameField, gameField.status, gameField.currentPlayer)
    publish(new GameFieldChanged)
    newField
}
