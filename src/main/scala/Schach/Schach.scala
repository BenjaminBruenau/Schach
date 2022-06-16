package Schach

import com.google.inject.Guice
import aview.gui.Gui
import aview.{RestUI, Tui}
import controller.controllerComponent._


@main def SchachMain(): Unit = {

  var break = false
  val injector = Guice.createInjector(new GameFieldModule)
  val controller = injector.getInstance(classOf[ControllerInterface])
  controller.createGameField()
  val gui = Gui(controller)
  val tui = Tui(controller)
  val restUi = RestUI(controller)
  restUi.startServer()

  println("Move the chess pieces: position they are at now -> position they should go to")
  println("Create a new GameField with 'new'")
  println("Usage example: move A2 A3")
  println("Type 'exit' to leave\n")

  gui.update()
  tui.update()

  while (!break) {
    val line = scala.io.StdIn.readLine()

    if (line != null && line.equals("exit")) {
      break = true
      println("exiting...")
    } else {
      if (line != null) tui.interactWithUser(line)
    }
  }
  
}
