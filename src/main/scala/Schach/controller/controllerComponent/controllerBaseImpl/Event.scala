package Schach.controller.controllerComponent.controllerBaseImpl

import scala.swing.event.Event

class GameFieldChanged extends Event

case class StatusChanged(statusID: Int, player: String) extends Event

case class ExceptionOccurred(exception: Throwable) extends Event