package model.gameModel.gameFieldComponent

import model.gameModel.figureComponent.*
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import spray.json.*

import java.awt.Color
import scala.util.{Failure, Try, Success}

trait GameFieldJsonProtocol extends DefaultJsonProtocol {


  implicit val gameStatusFormat: RootJsonFormat[GameStatus] = new RootJsonFormat[GameStatus] {
    override def read(json: JsValue): GameStatus = {
      json.asJsObject.getFields("game_status") match {
        case Seq(JsNumber(value)) => GameStatus.fromOrdinal(value.toInt)
        case unrecognized => deserializationError(s"json serialization error $unrecognized")
      }
    }

    override def write(obj: GameStatus): JsValue = {
      JsObject(
        "game_status" -> JsNumber(obj.value)
      )
    }
  }

  implicit val figureFormat: RootJsonFormat[Figure] = new RootJsonFormat[Figure] {
    override def read(json: JsValue): Figure = {
      val fields = json.asJsObject.fields
      getPiece(
        fields("piece").convertTo[String],
        fields("xPos").convertTo[Int],
        fields("yPos").convertTo[Int],
        fields("moved").convertTo[String]
      )
    }

    override def write(piece: Figure): JsValue = {
      JsObject(
        "piece" -> JsString(piece.toString),
        "xPos" -> JsNumber(piece.x),
        "yPos" -> JsNumber(piece.y),
        "moved" -> JsString(piece match {
          case pawn: Pawn => pawn.hasBeenMoved.toString
          case king: King => king.hasBeenMoved.toString
          case rook: Rook => rook.hasBeenMoved.toString
          case _ => ""
        })
      )
    }
  }

  implicit val colorFormat: RootJsonFormat[Color] = new RootJsonFormat[Color] {
    override def read(json: JsValue): Color = {
      json.asJsObject.getFields("currentPlayer") match {
        case Seq(JsString(value)) => getColor(value)
        case unrecognized => deserializationError(s"json serialization error $unrecognized")
      }
    }

    override def write(obj: Color): JsValue = {
      JsObject(
        "currentPlayer" -> JsString(obj.toString)
      )
    }
  }

  implicit val gameFieldFormat: RootJsonFormat[GameField] = jsonFormat3(GameField)

  implicit val tupleFormat: RootJsonFormat[(Long, GameField)] = new RootJsonFormat[(Long, GameField)] :
    override def read(json: JsValue): (Long, GameField) =
      val fields = json.asJsObject.fields
      (fields("id").convertTo[Long], fields("game").convertTo[GameField])

    override def write(obj: (Long, GameField)): JsValue =
      JsObject(
        "id" -> JsNumber(obj._1),
        "game" -> obj._2.toJson
      )

  private def getPiece(figure: String, x: Int, y: Int, moved: String): Figure = {
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
      case "♚" => moved.toBoolean match {
        case true => King(x, y, Color.BLACK, Some(true))
        case false => King(x, y, Color.BLACK)
      }
      case "♔" => moved.toBoolean match {
        case true => King(x, y, Color.WHITE, Some(true))
        case false => King(x, y, Color.WHITE)
      }
      case "♛" => Queen(x, y, Color.BLACK)
      case "♕" => Queen(x, y, Color.WHITE)
      case "♜" => moved.toBoolean match {
        case true => Rook(x, y, Color.BLACK, Some(true))
        case false => Rook(x, y, Color.BLACK)
      }
      case "♖" => moved.toBoolean match {
        case true => Rook(x, y, Color.WHITE, Some(true))
        case false => Rook(x, y, Color.WHITE)
      }
    }
  }

  private def getColor(s: String): Color = {
    s match {
      case "java.awt.Color[r=0,g=0,b=0]" => Color.BLACK
      case "java.awt.Color[r=255,g=255,b=255]" => Color.WHITE
    }
  }

}
