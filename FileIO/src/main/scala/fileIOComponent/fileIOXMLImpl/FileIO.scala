package fileIOComponent.fileIOXMLImpl

import fileIOComponent.FileIOInterface

import java.awt.Color

class FileIO extends FileIOInterface{

  override def loadGame: (Vector[Figure], Color) = {

    val file = scala.xml.XML.loadFile("save.xml")
    val player = (file \\ "@player").text
    val figureNodes = (file \\ "figure")
    var figureVec: Vector[Figure] = Vector.empty[Figure]
    for (figure <- figureNodes) {
      val x = (figure \ "@xPos").text.toInt
      val y = (figure \ "@yPos").text.toInt
      val moved = (figure \ "@moved").text
      val fig = (figure \ "@value").text

      if !fig.equals("") then
        val piece = getPiece(fig, x, y, moved)
        figureVec = figureVec :+ piece
    }
    (figureVec, getColor(player))
  }

  override def saveGame(gameFieldBuilder: ChessGameFieldBuilderInterface): Vector[Figure] = {
    val printWriter = new PrintWriter(new File("save.xml"))
    val prettyPrinter = new PrettyPrinter(120, 4)
    val xml = prettyPrinter.format(gameFieldToXML(gameFieldBuilder.getGameField))
    printWriter.write(xml)
    printWriter.close()
    gameFieldBuilder.getGameField.gameField
  }

  def gameFieldToXML(gameField: GameField): Elem = {
    <gameField player={gameField.currentPlayer.toString}>
      {
        for {
          xPos <- 0 until 8
          yPos <- 0 until 8
        } yield figureToXML(gameField, xPos, yPos)
      }
    </gameField>
  }

  def figureToXML(gameField: GameFieldInterface, xPos: Int, yPos: Int): Elem = {

    val figure = gameField.getFigure(xPos, yPos)
    val xmlFig = {
      <figure xPos={xPos.toString} yPos={yPos.toString} moved={getCorrectString(figure)}
      value={
      figure match {
        case Some(value) => value.toString
        case None => ""
      }}>
      </figure>
    }
    xmlFig
  }

}
