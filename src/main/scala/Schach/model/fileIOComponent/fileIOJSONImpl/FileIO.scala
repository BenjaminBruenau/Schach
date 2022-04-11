package Schach.model.fileIOComponent.fileIOJSONImpl

import Schach.model.figureComponent.*
import Schach.model.fileIOComponent.FileIOInterface
import Schach.model.gameFieldComponent.ChessGameFieldBuilderInterface
import Schach.model.gameFieldComponent.gameFieldBaseImpl.GameField
import play.api.libs.json.*

import java.awt.Color
import java.io.*
import scala.io.Source

class FileIO extends FileIOInterface {

  override def loadGame: (Vector[Figure], Color) = {

    val file = Source.fromFile("save.json").getLines().mkString
    val json = Json.parse(file)
    val player = (json \ "field" \ "player").as[String]
    var figureVec: Vector[Figure] = Vector.empty[Figure]
    for (idx <- 0 until 8 * 8) {
      val x = (json \\ "xPos")(idx).as[Int]
      val y = (json \\ "yPos")(idx).as[Int]
      val moved = (json \\ "moved")(idx).as[String]
      val fig = (json \\ "figure")(idx).as[String]

      if !fig.equals("") then
        val piece = getPiece(fig, x, y, moved)
        figureVec = figureVec :+ piece
    }
    (figureVec, getColor(player))
  }

  override def saveGame(gameFieldBuilder: ChessGameFieldBuilderInterface): Vector[Figure] = {
    val printWriter = new PrintWriter(new File("save.json"))
    printWriter.write(Json.prettyPrint(gameFieldToJSON(gameFieldBuilder.getGameField)))
    printWriter.close()
    gameFieldBuilder.getGameField.gameField
  }

  def gameFieldToJSON(field: GameField): JsObject = {
    Json.obj(
      "field" -> Json.obj(
 "player" -> Json.toJson(field.currentPlayer.toString),
        "cells" -> Json.toJson(
          for {
            xPos <- 0 until 8
            yPos <- 0 until 8
          } yield {
            Json.obj(
              "xPos" -> xPos,
              "yPos" -> yPos,
              "figure" -> Json.toJson(field.getFigure(xPos, yPos) match {
                case Some(value) => value.toString
                case None => ""
              }),
              "moved" -> Json.toJson(getCorrectString(field.getFigure(xPos, yPos)))
            )
          }
        )
      )
    )
  }


}
