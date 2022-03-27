package Schach.controller.controllerComponent.controllerBaseImpl

import scala.swing.event.Event

class GameFieldChanged extends Event

class StatusChanged(statusID: Int, player: String) extends Event {
  def getStatusID: Int = {
    statusID
  }

  def getPlayer: String = {
    player
  }
}