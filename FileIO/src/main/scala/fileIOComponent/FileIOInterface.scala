package fileIOComponent

import gameManager.ChessGameFieldBuilderInterface
import model.figureComponent.{Figure, Pawn}

import java.awt.Color

trait FileIOInterface {

  def loadGame: (Vector[Figure], Color)

  def saveGame(gameField: ChessGameFieldBuilderInterface): Vector[Figure]


  def getCorrectString(piece: Option[Figure]): String = {
    piece match {
      case Some(value) =>
        value match {
          case pawn: Pawn =>
            pawn.hasBeenMoved.toString
          case _ => ""
        }
      case None => ""
    }
  }

  def getColor(s: String): Color = {
    s match {
      case "java.awt.Color[r=0,g=0,b=0]" => Color.BLACK
      case "java.awt.Color[r=255,g=255,b=255]" => Color.WHITE
    }
  }

  def getPiece(figure: String, x: Int, y: Int, moved: String) = {
    figure match {
      case "♟" => moved.toBoolean match {
        case true => Pawn(x, y, Color.BLACK, Some(true))
        case false => Pawn(x, y, Color.BLACK)
      }
      case "♙" => moved.toBoolean match {
        case true => Pawn(x, y, Color.WHITE, Some(true))
        case false => Pawn(x, y, Color.WHITE)
      }
      case "♝" => Bishop(x, y, Color.BLACK)
      case "♗" => Bishop(x, y, Color.WHITE)
      case "♞" => Knight(x, y, Color.BLACK)
      case "♘" => Knight(x, y, Color.WHITE)
      case "♚" => King(x, y, Color.BLACK)
      case "♔" => King(x, y, Color.WHITE)
      case "♛" => Queen(x, y, Color.BLACK)
      case "♕" => Queen(x, y, Color.WHITE)
      case "♜" => Rook(x, y, Color.BLACK)
      case "♖" => Rook(x, y, Color.WHITE)
    }
  }


}
