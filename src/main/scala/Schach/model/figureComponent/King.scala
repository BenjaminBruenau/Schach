package Schach.model.figureComponent

import java.awt.Color

case class King(x: Int, y : Int, color: Color) extends Figure {
  override val name: String = this.getClass.getSimpleName


  override def toString: String = {
    color match {
      case Color.BLACK => "♚"
      case Color.WHITE => "♔"
    }
  }
}