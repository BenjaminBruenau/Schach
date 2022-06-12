package Schach.util

import model.gameModel.figureComponent.Figure

trait Originator {
  def save(): Unit
  def restore(): Vector[Figure]
}
