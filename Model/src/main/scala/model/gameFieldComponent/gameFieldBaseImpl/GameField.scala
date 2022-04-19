package model.gameFieldComponent.gameFieldBaseImpl

import java.awt.Color
import scala.collection.immutable.{List, Range, Vector}
import scala.util.control.Breaks
import scala.util.{Failure, Success, Try}

/** The GameField of our Chess Game, realized as a Vector of Figures
 *
 * @param gameField - The Vector which is keeping track of all the moves etc.
 */
case class GameField(gameField: Vector[Figure], status: GameStatus, currentPlayer: Color) extends GameFieldInterface {

  def this() = this(Vector(), GameStatus.Running, Color.WHITE)

  def addFigures(figures: Vector[Figure]): Vector[Figure] = {
    for in
    <- gameField
    do
      if figures.contains(in) then
    return gameField

    gameField.appendedAll(figures)
  }

  def getFigures: Vector[Figure] = gameField

  def convertFigure(figure: Figure, toFigure: Figure): Vector[Figure] =
    gameField.filter(!_.equals(figure)) :+ toFigure

  def moveTo(xNow: Int, yNow: Int, xNext: Int, yNext: Int): Vector[Figure] = {
    Try(getFigure(xNow, yNow).get) match {
      case Success(figure) =>
        getFigure(xNext, yNext) match {
          case Some(fig) => fig.checked = true
          case None =>
        }
        figure match {
          case _: Pawn => gameField.filter(!_.equals(figure)) :+ Pawn(xNext, yNext, figure.color, Some(true))
          case _: Rook => gameField.filter(!_.equals(figure)) :+ Rook(xNext, yNext, figure.color, Some(true))
          case _: Knight => gameField.filter(!_.equals(figure)) :+ Knight(xNext, yNext, figure.color)
          case _: Bishop => gameField.filter(!_.equals(figure)) :+ Bishop(xNext, yNext, figure.color)
          case _: Queen => gameField.filter(!_.equals(figure)) :+ Queen(xNext, yNext, figure.color)
          case king: King =>
            if (king.aboutToRochade) executeRochade(king, xNext, yNext)
            gameField.filter(!_.equals(figure)) :+ King(xNext, yNext, figure.color, Some(true))
        }
      case Failure(_) => gameField
    }
  }

  private def executeRochade(king: King, xNext: Int, yNext: Int): Unit = {
    println("Executing Rochade")
    (xNext, yNext) match {
      case (6, 0) => moveTo(7, 0, 5, 0) // short rochade white
      case (2, 0) => moveTo(0, 0, 3, 0) // long rochade white
      case (6, 7) => moveTo(7, 7, 5, 7) // short rochade black
      case (2, 7) => moveTo(0, 7, 3, 7) // long rochade black
      case _ => println("Fatal Rochade Error")
    }
    king.aboutToRochade = false // not necessary since new instance is created anyway
  }

  def moveValid(xNow: Int, yNow: Int, xNext: Int, yNext: Int): Boolean = {
    getFigure(xNow, yNow) match {
      case Some(value) =>
        if value.color != currentPlayer then
          println("Wrong player")
        return false
      case None => return false
    }

    val rule = Rules(this)
    rule.moveValidFigure(xNow, yNow, xNext, yNext)
  }

  def moveToFieldAllowed(x: Int, y: Int, figure: Figure): Boolean = {
    val check1 = !setSelfIntoCheck(figure, x, y)

    getFigure(x, y) match {
      case Some(figure2) =>
        val check2 = !figure2.isInstanceOf[King] && figure2.color != figure.color
        check1 && check2

      case None => check1
    }
  }

  def setSelfIntoCheck(figure: Figure, xNext: Int, yNext: Int): Boolean = {
    val figureTo = getFigure(xNext, yNext)
    if figureTo.isDefined then figureTo.get.checked = true

    // simulate move
    val fieldAfterMove = moveTo(figure.x, figure.y, xNext, yNext)

    val king = fieldAfterMove.filter(_.color == figure.color).find(_.isInstanceOf[King]).get
    val figuresEnemy = fieldAfterMove.filter(!_.checked).filter(_.color != king.color)

    val output = validateRules(figuresEnemy, king)

    figure match {
      case pawn: Pawn =>
        if (!pawn.hasBeenMoved)
          Try(getFigure(figure.x, figure.y).get.asInstanceOf[Pawn]) match {
            case Success(value) => value.hasBeenMoved = false
            case Failure(exception) => print("Failure while trying to access Pawn")
          }
      case _ =>
    }
    if figureTo.isDefined then figureTo.get.checked = false
    output
  }

  def pawnHasReachedEnd(): Boolean = {
    val pawns = gameField.filter(_.isInstanceOf[Pawn])
    pawns.exists(figure => figure.y == 7 || figure.y == 0)
  }

  def getPawnAtEnd(): Pawn = {
    val pawnAtEnd = gameField.filter(_.isInstanceOf[Pawn]).filter(figure => figure.y == 0 || figure.y == 7)
    pawnAtEnd.head.asInstanceOf[Pawn]
  }

  def isChecked(playerCol: Color): Boolean = {
    val figuresEnemy = getFigures.filter(!_.checked).filter(_.color != playerCol)
    val myKing = getFigures.filter(_.color == playerCol).filter(_.isInstanceOf[King])(0)

    validateRules(figuresEnemy, myKing)
  }

  def isCheckmate(playerCol: Color): Boolean = {
    val myKing = getFigures.filter(_.color == playerCol).filter(_.isInstanceOf[King])(0)
    val cellFreeAround = cellsFreeAroundFigure(myKing)
    val loop = new Breaks
    val figuresEnemy = getFigures.filter(!_.checked).filter(_.color != myKing.color)
    var cellValidKing: List[Boolean] = List()

    for cell
    <- cellFreeAround
    do
      moveTo(myKing.x, myKing.y, cell._1, cell._2)
    val rules = Rules(this)
    var added = false
    loop.breakable {
      for fig
      <- figuresEnemy
      do
        if rules.moveValidWithoutKingCheck(fig.x, fig.y, cell._1, cell._2) then
          cellValidKing = cellValidKing :+ false
      added = true
      loop.break
    }
    if !added then cellValidKing = cellValidKing :+ true
    moveTo(cell._1, cell._2, myKing.x, myKing.y)


    val back = cellValidKing.contains(true) || cellValidKing.isEmpty
    !back
  }

  def cellsFreeAroundFigure(figure: Figure): List[(Int, Int)] = {
    var validMoves: List[(Int, Int)] = List()

    for x
    <- Range(-1, 2, 1)
    do
      for y
    <- Range(-1, 2, 1)
    do
    val point = (figure.x + x, figure.y + y)
    getFigure(point._1, point._2) match {
      case Some(_) =>
      case None =>
        if point._1 >= 0 && point._1 < 8 && point._2 >= 0 && point._2 < 8 then
          validMoves = validMoves :+ point
    }
    validMoves
  }

  def wayToIsFreeStraight(xNow: Int, yNow: Int, xNext: Int, yNext: Int): Boolean = {
    if xNow == xNext && yNow == yNext then
    return false

    //vertical move
    if xNow == xNext then
    val incY = yNow > yNext match {
      case x if x => -1
      case _ => 1
    }
    for y
    <- Range(yNow + incY, yNext, incY)
    do
      if gameField.exists(input => input.y == y && input.x == xNow) then
    return false
    return true


    //horizontal move
    else if yNow == yNext then
    val incX = yNow > yNext match {
      case x if x => -1
      case _ => 1
    }

    for x
    <- Range(xNow + incX, xNext, incX)
    do
      if gameField.exists(input => input.x == x && input.y == yNow) then
    return false

    return true

    false

  }

  def wayToIsFreeDiagonal(xNow: Int, yNow: Int, xNext: Int, yNext: Int): Boolean = {
    if (Math.abs(xNow - xNext) != Math.abs(yNow - yNext)) ||(xNow == xNext || yNow == yNext) then
    return false

    val incX = xNext < xNow match {
      case x if x => -1
      case _ => 1 //standard move diagonal right up
    }

    val incY = (xNext < xNow && yNext < yNow) || yNext < yNow match {
      case x if x => -1 //move down
      case _ => 1
    }

    var y = yNow + incY

    for x
    <- Range(xNow + incX, xNext, incX)
    do
      if !gameField.exists(input => input.x == x && input.y == y) then y += incY
      else return false

    true
  }

  def getFigure(xPos: Int, yPos: Int): Option[Figure] =
    gameField.filter(_.checked == false).filter(_.x == xPos).find(_.y == yPos)

  private def validateRules(enemyPieces: Vector[Figure], myKing: Figure): Boolean = {
    val rules = Rules(this)
    for fig
    <- enemyPieces
    do
      if rules.moveValidWithoutKingCheck(fig.x, fig.y, myKing.x, myKing.y) then
    return true
    false
  }

  override def toString: String = {
    val build = new StringBuilder
    build.append("\tA\tB\tC\tD\tE\tF\tG\tH\n")
    build.append("\t──────────────────────────────\n")

    for y
    <- Range(7, -1, -1)
    do
      build.append(y + 1).append(" │\t")

    val row = gameField.filter(!_.checked).filter(_.y == y)

    for x
    <- 0 to 7
    yield row.find(_.x == x) match {
      case Some(piece) => build.append(piece.toString + "\t")
      case None => build.append("─\t")
    }
    build.append("\n")

    build.toString
  }

}
