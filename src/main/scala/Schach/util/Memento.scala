package Schach.util

import model.gameModel.figureComponent.Figure
import java.awt.Color

trait Memento {
  def getFigures: Vector[Figure]
  def getPlayer: Color
}
