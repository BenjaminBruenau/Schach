package Schach.aview

import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.controllerBaseImpl.{ExceptionOccurred, GameFieldChanged}
import persistence.RetryExceptionList

import java.awt.Color
import scala.swing.Reactor

class Tui(controller: ControllerInterface) extends Reactor {
  var lastOutput: String = "";

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
      case "new" => controller.createGameField()
      case "move" =>
        if (args.size == 3 && controller.controlInput(args(1)) && controller.controlInput(args(2))) {
          val command = args(1).concat(" ").concat(args(2))
          controller.movePiece(readInput(command))
        }
        else {
          tuiPrint("Wrong Input: Invalid Move")
        }
      case "switch" => tuiPrint(convertPawn(args(1)))
      case "undo" => controller.undo()
      case "redo" => controller.redo()
      case "save" => controller.save()
      case "load" =>
        if (controller.caretakerIsCalled()){
          controller.restore()
        } else {
          tuiPrint("No Save created yet")
        }
      case "save_game" => controller.saveGame()
      case "load_game" => controller.loadLastSave()
      case _ => tuiPrint("No Valid Command")
    }
  }

  def tuiPrint(stringToPrint: String): String =
    println(stringToPrint)
    val tmp = lastOutput
    lastOutput = stringToPrint
    tmp

  def readInput(line: String): Vector[Int] = controller.readInput(line)
  
  def getPoint(input: Char): Int = controller.getPoint(input)
  
  def printGameStatus(): String = controller.printGameStatus()

  def convertPawn(line: String): String = {
    controller.changePlayer()
    val string = {if (controller.getPlayer().getRed == 0) "Black's "
            else "White's "} + "pawn has reached the end of the game field.\n" +
            "Change it to a 'queen', 'rook', 'knight' or 'bishop' by typing into the console"

    val result =
    line match {
      case "queen" => controller.convertPawn("queen")
      case "rook" => controller.convertPawn("rook")
      case "knight" => controller.convertPawn("knight")
      case "bishop" => controller.convertPawn("bishop")
      case _ => None
    }

    controller.refreshStatus()
    val resultString =
      if result.nonEmpty then string + printGameStatus()
      else string + "\n" + "Wrong Input \n" + printGameStatus()
    controller.changePlayer()
    resultString
  }

   def update(): String =
    tuiPrint(printGameStatus())
    tuiPrint(controller.gameFieldToString)

   def printError(exception: Throwable): Unit =
     val errorString = "\nERROR OCCURED:\n" + exception.asInstanceOf[RetryExceptionList].list.last._2.getMessage + "\n"
     tuiPrint(errorString)
   
}