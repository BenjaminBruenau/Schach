package Schach

import aview.Tui
import controller.Controller
import model._


object Schach {


  def main(args: Array[String]) {
    /*
    println("Welcome to Chess")
    println("What is your name?")
    val name = scala.io.StdIn.readLine()

    val playerOne = Player(name)
    println("Hello " + playerOne.name + "\n")
    println("This is the Chess Board\n")
    val field = createGameField()
    println(field)
    println(field.length)
    */

    var break = false
    val builder = new ChessGameFieldBuilder
    builder.makeGameField()
    val controller = new Controller(builder.getGameField)
    val tui = new Tui(controller)
    controller.notifyObservers
    //println(controller.gameFieldToString)
    /*
    val gameField = new GameField().moveTo(0, 0, 0, 6)
    println(gameField.toString)
     */

    println("Move the chess pieces: position they are at now -> position they should go to")
    println("Create a new GameField with 'new'")
    println("Usage example: move A2 A3")
    println("Type 'exit' to leave\n")

    while (!break) {
      val line = scala.io.StdIn.readLine()

      if (line.equals("exit")) {
        break = true
        println("exiting...")
      } else {
        tui.interactWithUser(line)
      }
    }
  }
}