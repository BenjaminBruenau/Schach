package Schach.util

import java.awt.Color

trait Memento {
  def getFigures: Vector[Figure]
  def getPlayer: Color
}
