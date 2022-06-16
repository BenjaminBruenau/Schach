package Schach.controller.controllerComponent.controllerBaseImpl

import Schach.GameFieldModule
import Schach.aview.RestUI
import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.api.HttpServiceInterface
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
import model.gameManager.ChessGameFieldBuilderInterface
import model.gameModel.figureComponent.*
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import model.gameModel.gameFieldComponent.{GameFieldInterface, GameFieldJsonProtocol, GameStatus}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import spray.json.*

import java.awt.Color
import java.util.concurrent.TimeUnit
import scala.collection.immutable.Vector
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

class Controller @Inject() (httpService: HttpServiceInterface) extends ControllerInterface {
  val undoManager = new UndoManager
  val caretaker = new Caretaker
  val awaitDuration: FiniteDuration = Duration.apply(10000, TimeUnit.MILLISECONDS)
  var gameField: GameField = GameField(Vector.empty, GameStatus.Running, Color.WHITE)

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global


  def createGameField(): Vector[Figure] = {
    val makeGameFieldFuture = httpService.makeGameFieldViaHttp
    makeGameFieldFuture.onComplete {
      case Success(newGameField) =>
        gameField = newGameField
        publish(new GameFieldChanged)
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(makeGameFieldFuture, awaitDuration).gameField
  }

  def controlInput(line: String): Boolean = line.matches("[A-H][1-8]")

  def gameFieldToString: String = gameField.toString

  def getGameField: Vector[Figure] = gameField.getFigures

  def movePiece(newPos: Vector[Int]): Boolean = {
    if (moveIsValid(newPos)) {
      undoManager.doStep(new MoveCommand(newPos(0), newPos(1), newPos(2), newPos(3), this))
      changePlayer()
      refreshStatus()
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

    if (gameField.pawnHasReachedEnd())
      setGameStatus(GameStatus.PawnReachedEnd)

    getGameStatus()
  }

  def moveIsValid(newPos: Vector[Int]): Boolean = {
    val valid = gameField.moveValid(newPos(0), newPos(1), newPos(2), newPos(3))

    if (valid) setGameStatus(GameStatus.Running)
    else setGameStatus(GameStatus.MoveIllegal)

    valid
  }

  def getGameStatus() : Int = gameField.status.value

  def setGameStatus(newState : GameStatus): GameStatus =
    val statusFuture = httpService.updateStatusViaHttp(newStatus = newState)
    statusFuture.onComplete {
      case Success(newGameField) =>
        gameField = newGameField
        publish( new GameFieldChanged )
        publish( StatusChanged(getGameStatus(), { if (getPlayer().getRed == 0) "BLACK" else "WHITE" }) )
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(statusFuture, awaitDuration).status

  def getPlayer(): Color = gameField.currentPlayer

  def setPlayer(color: Color): Color =
    val updatePlayerFuture = httpService.updatePlayerViaHttp(newPlayer = color)
    updatePlayerFuture.onComplete {
      case Success(newGameField) =>
        gameField = newGameField
        publish( new GameFieldChanged )
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(updatePlayerFuture, awaitDuration).currentPlayer

  def changePlayer(): Color = {
    gameField.currentPlayer match {
      case Color.BLACK => setPlayer(Color.WHITE)
      case Color.WHITE => setPlayer(Color.BLACK)
    }
  }

  def clear() : Boolean =
    val updateGameFieldFuture = httpService.updateGameFieldViaHttp((Vector.empty, GameStatus.Running, Color.WHITE))
    updateGameFieldFuture.onComplete {
      case Success(newGameField) => gameField = newGameField
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(updateGameFieldFuture, awaitDuration).gameField.isEmpty

  def convertPawn(figureType : String): Option[Figure] = {
    Try(gameField.getPawnAtEnd()) match {
      case Success(pawn) =>
        val (newField, convertedPiece) = figureType match {
          case "queen" =>
            (gameField.convertFigure(pawn, Queen(pawn.x, pawn.y, pawn.color)),
            Queen(pawn.x, pawn.y, pawn.color))
          case "rook" =>
            (gameField.convertFigure(pawn, Rook(pawn.x, pawn.y, pawn.color)),
            Rook(pawn.x, pawn.y, pawn.color))
          case "knight" =>
            (gameField.convertFigure(pawn, Knight(pawn.x, pawn.y, pawn.color)),
            Knight(pawn.x, pawn.y, pawn.color))
          case "bishop" =>
            (gameField.convertFigure(pawn, Bishop(pawn.x, pawn.y, pawn.color)),
            Bishop(pawn.x, pawn.y, pawn.color))
          case _=> return None
        }
        val updateFieldFuture = httpService.updateFieldViaHttp(newField = newField)
        updateFieldFuture.onComplete {
          case Success(newGameField) =>
            gameField = newGameField
            publish(new GameFieldChanged)
          case Failure(exception) => publish(ExceptionOccurred(exception))
        }
        Await.ready(updateFieldFuture, awaitDuration)
        Some(convertedPiece)
      case Failure(_) => println("No Pawn Reached the End!")
      None
    }

  }

  def updateGameField(newField : Vector[Figure]): Vector[Figure] =
    val updateFieldFuture = httpService.updateFieldViaHttp(newField)
    updateFieldFuture.onComplete {
      case Success(newGameField) =>
        gameField = newGameField
        publish(new GameFieldChanged)
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(updateFieldFuture, awaitDuration).gameField

  def isChecked(): Boolean = gameField.isChecked(getPlayer())

  def isCheckmate(): Boolean = gameField.isCheckmate(getPlayer())

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

  def restore(): Vector[Figure] = {
    clear()
    val updateFieldFuture = httpService.updateFieldViaHttp(newField = caretaker.getMemento.getFigures)
    updateFieldFuture.onComplete {
      case Success(newGameField) =>
        gameField = newGameField
        publish(new GameFieldChanged)
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(updateFieldFuture, awaitDuration).gameField
  }

  def caretakerIsCalled(): Boolean = caretaker.called

  def saveGame(): Vector[Figure] =
    val saveGameFuture = httpService.saveGameViaHttp(gameField)
    saveGameFuture.onComplete {
      case Success(_) => println("Successfully Saved Game")
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.ready(saveGameFuture, awaitDuration)
    getGameField

  def loadGame(): Vector[Figure] = {
    clear()
    val loadGameFuture = httpService.loadGameViaHttp(1.toLong)
    loadGameFuture.onComplete {
      case Success(newGameField) => replaceGameField(newGameField)
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    httpService.futureHandler.resolveFutureBlocking(loadGameFuture).gameField
  }
  
  def loadLastSave(): Vector[Figure] = {
    val gameSavesFuture = httpService.getGameSavesViaHttp
    gameSavesFuture.onComplete{
      case Success(saves) => replaceGameField(saves.last._2)
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    httpService.futureHandler.resolveFutureBlocking(gameSavesFuture).last._2.gameField
  }

  def listSaves(): Vector[(Long, GameField)] =
    val gameSavesFuture = httpService.getGameSavesViaHttp
    gameSavesFuture.onComplete{
      case Success(_) => println("Successfully Loaded Saves")
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    httpService.futureHandler.resolveFutureBlocking(gameSavesFuture)


  def replaceGameField(field: GameField): GameField =
    val replaceGameFieldFuture = httpService.updateGameFieldViaHttp(field.gameField, field.status, field.currentPlayer)
    replaceGameFieldFuture.onComplete {
      case Success(newGameField) =>
        gameField = newGameField
        publish(new GameFieldChanged)
      case Failure(exception) => publish(ExceptionOccurred(exception))
    }
    Await.result(replaceGameFieldFuture, awaitDuration)

  def getGameFieldAsync: Vector[Figure] =
    val getGameFieldFuture = httpService.getGameFieldViaHttp
    httpService.futureHandler.resolveFutureBlocking(getGameFieldFuture).gameField
  
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
