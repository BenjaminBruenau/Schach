package Schach.aview

import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.controllerBaseImpl.{ExceptionOccurred, GameFieldChanged}
import persistence.RetryExceptionList

import scala.swing.Reactor
import scala.util.Success

class Tui(controller: ControllerInterface) extends Reactor {

  listenTo(controller)

  reactions += {
    case _ : GameFieldChanged => update()
    case ExceptionOccurred(exception) => printError(exception)
  }

  /**
   *
   * @param input
   */
  def interactWithUser(input: String) : Unit = {
    val args = input.split(" ")

    args(0) match {
      case "new" => Success(controller.createGameField())
      case "move" =>
        if (args.size == 3 && controller.controlInput(args(1)) && controller.controlInput(args(2))) {
          val command = args(1).concat(" ").concat(args(2))
          controller.movePiece(readInput(command))
        }
        else {
          println("Wrong Input: Invalid Move")
        }
      case "switch" => convertPawn(args(1))
      case "undo" => controller.undo()
      case "redo" => controller.redo()
      case "save" => controller.save()
      case "load" =>
        if (controller.caretakerIsCalled()){
          controller.restore()
        } else {
          println("No Save created yet")
        }
      case "save_game" => controller.saveGame()
      case "load_game" => controller.loadGame()
      case _ => println("No Valid Command")
    }
  }

  def readInput(line: String): Vector[Int] = controller.readInput(line)
  
  def getPoint(input: Char): Int = controller.getPoint(input)
  
  def printGameStatus(): Unit = {
    controller.getGameStatus() match {
      case 0 => println("RUNNING")
      case 1 => println("PLAYER " + { if (controller.getPlayer().getRed == 0) "Black"
                                      else "WHITE"} + "IS CHECKED")
      case 2 => println({if (controller.getPlayer().getRed == 0) "BLACK "
                          else "WHITE "} + "IS CHECKMATE")
      case 3 => println("INVALID MOVE")
      case 4 =>
      //println("PAWN HAS REACHED THE END")
    }
  }

  def convertPawn(line: String) = {
    controller.changePlayer()
    println({if (controller.getPlayer().getRed == 0) "Black's "
            else "White's "} + "player has reached the end of the game field.\n" +
            "Change it to a 'queen', 'rook', 'knight' or 'bishop' by typing into the console")

    line match {
      case "queen" => controller.convertPawn("queen")
      case "rook" => controller.convertPawn("rook")
      case "knight" => controller.convertPawn("knight")
      case "bishop" => controller.convertPawn("bishop")
      case _ => println("Wrong Input")
    }

    controller.refreshStatus()
    printGameStatus()
    controller.changePlayer()
  }

   def update(): Unit = {
    printGameStatus()
    println(controller.gameFieldToString)
  }

   def printError(exception: Throwable) = {
     println("\nERROR OCCURED:")
     println(exception.asInstanceOf[RetryExceptionList].list.last._2.getMessage)
     println()
   }
}