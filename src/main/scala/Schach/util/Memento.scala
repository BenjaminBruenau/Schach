package Schach.util

import model.figureComponent.Figure

import java.awt.Color

trait Memento {
  def getFigures: Vector[Figure]
  def getPlayer: Color
}
