package model.gameModel.figureComponent

import java.awt.Color

case class Rook(x: Int, y : Int, color: Color, moved: Option[Boolean] = None) extends Figure {
  override val name: String = this.getClass.getSimpleName

  if (moved.contains(true)) hasBeenMoved = true

  override def toString: String = {
    color match {
      case Color.BLACK => "♜"
      case Color.WHITE => "♖"
    }
  }
}
