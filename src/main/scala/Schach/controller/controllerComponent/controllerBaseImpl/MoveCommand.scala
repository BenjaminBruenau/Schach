package Schach.controller.controllerComponent.controllerBaseImpl

import Schach.util.Command

class MoveCommand(xNow: Int, yNow: Int, xNext: Int, yNext: Int, controller: Controller) extends Command {
  var memento = new GameFieldMemento(controller.getGameField, controller.getPlayer())

  override def doStep(): Unit = {
    memento = new GameFieldMemento(controller.getGameField, controller.getPlayer())
    controller.updateGameField(controller.gameFieldBuilder.getGameField.moveTo(xNow, yNow, xNext, yNext))
  }


  override def undoStep(): Unit = {
    controller.clear()
    controller.updateGameField(memento.getFigures)
    controller.setPlayer(memento.getPlayer)
  }


  override def redoStep(): Unit = {
    controller.updateGameField(controller.gameFieldBuilder.getGameField.moveTo(xNow, yNow, xNext, yNext))
    controller.changePlayer()
  }

}
