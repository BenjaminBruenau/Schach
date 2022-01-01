package Schach.model.figureComponent

import java.awt.Color

case class Pawn(x: Int, y : Int, color: Color, moved: Option[Boolean] = None) extends Figure {
  override val name: String = this.getClass.getSimpleName

  var hasBeenMoved = false
  if (moved.contains(true)) hasBeenMoved = true

  override def toString: String = {
    color match {
      case Color.BLACK => "♟"
      case Color.WHITE => "♙"
    }
  }
}
