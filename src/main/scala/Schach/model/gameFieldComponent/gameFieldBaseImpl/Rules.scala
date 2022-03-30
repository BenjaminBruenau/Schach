package Schach.model.gameFieldComponent.gameFieldBaseImpl

import java.awt.Color
import Schach.model.figureComponent._

import scala.util.{Failure, Success, Try}

/** Connects the Chess Rules to a [[GameField]]
 *
 * @param gameField [[GameField]] to which the Rules are applied
 */
case class Rules(gameField: GameField) {
  /** Calls specific Rules for the Figure in question
   *
   * @param xNow  x-Axis Position on the GameField
   * @param yNow  y-Axis Position on the GameField
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def moveValidFigure(xNow: Int, yNow: Int, xNext: Int, yNext: Int): Boolean = {
    gameField.getFigure(xNow, yNow) match {
      case Some(figure: Pawn) => validPawn(figure, xNext, yNext)
      case Some(figure: Rook) => validRook(figure, xNext, yNext)
      case Some(figure: Knight) => validKnight(figure, xNext, yNext)
      case Some(figure: Bishop) => validBishop(figure, xNext, yNext)
      case Some(figure: Queen) => validQueen(figure, xNext, yNext)
      case Some(figure: King) => validKing(figure, xNext, yNext)
      case None => false
    }
  }

  /** Determines whether the specific Move results in Check.
   *
   *  In contradiction to the method name it inspects the move being valid to any position.
   *  But if the goal is to determine whether a King could possibly be checked the method is called like this:
   *  {{{
   *   validPawnWithoutKingCheck(figure, King.xPos, King.yPos)
   *  }}}
   * @param xNow  x-Axis Position on the GameField
   * @param yNow  y-Axis Position on the GameField
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if King isn't checked, otherwise false
   */
  def moveValidWithoutKingCheck(xNow: Int, yNow: Int, xNext: Int, yNext: Int): Boolean = {
    gameField.getFigure(xNow, yNow) match {
      case Some(figure: Pawn) => validPawnWithoutKingCheck(figure, xNext, yNext)
      case Some(figure: Rook) => validRookWithoutKingCheck(figure, xNext, yNext)
      case Some(figure: Knight) => validKnightWithoutKingCheck(figure, xNext, yNext)
      case Some(figure: Bishop) => validBishopWithoutKingCheck(figure, xNext, yNext)
      case Some(figure: Queen) => validQueenWithoutKingCheck(figure, xNext, yNext)
      case Some(figure: King) => validKingWithoutKingCheck(figure, xNext, yNext)
      case None => false
    }
  }

  /** See [[validPawnWithoutKingCheck]]
   */
  def validPawn(figure: Pawn, xNext: Int, yNext: Int): Boolean = {
    validPawnWithoutKingCheck(figure, xNext, yNext) && gameField.moveToFieldAllowed(xNext, yNext, figure)
  }

  /** See [[validRookWithoutKingCheck]]
   */
  def validRook(figure: Rook, xNext: Int, yNext: Int): Boolean = {
    validRookWithoutKingCheck(figure, xNext, yNext) && gameField.moveToFieldAllowed(xNext, yNext, figure)
  }

  /** See [[validKnightWithoutKingCheck]]
   */
  def validKnight(figure: Knight, xNext: Int, yNext: Int): Boolean = {
    validKnightWithoutKingCheck(figure, xNext, yNext) && gameField.moveToFieldAllowed(xNext, yNext, figure)
  }

  /** See [[validBishopWithoutKingCheck]]
   */
  def validBishop(figure: Bishop, xNext: Int, yNext: Int): Boolean = {
    validBishopWithoutKingCheck(figure, xNext, yNext) && gameField.moveToFieldAllowed(xNext, yNext, figure)
  }

  /** See [[validQueenWithoutKingCheck]]
   */
  def validQueen(figure: Queen, xNext: Int, yNext: Int): Boolean = {
    validQueenWithoutKingCheck(figure, xNext, yNext) && gameField.moveToFieldAllowed(xNext, yNext, figure)
  }

  /** See [[validKingWithoutKingCheck]]
   */
  def validKing(figure: King, xNext: Int, yNext: Int): Boolean = {
    validKingWithoutKingCheck(figure, xNext, yNext) && gameField.moveToFieldAllowed(xNext, yNext, figure)
  }


  /** Verifies if a Pawn is moving valid.
   *
   *  A Pawn can move 2 cells upward if it hasn't been moved yet, or 1 cell if it has.
   *
   *  It can only move 1 cell in the diagonal if there is an enemy Piece.
   *
   *  Pawns can never move backward.
   *
   *  See also [[moveValidWithoutKingCheck]]
   * @param figure The to be moved Pawn
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def validPawnWithoutKingCheck(figure: Pawn, xNext: Int, yNext: Int): Boolean = {
    //can't move backward
    if (figure.color == Color.BLACK && figure.y < yNext) || (figure.color == Color.WHITE && figure.y > yNext) then
      false

    //walks straight on
    else if Math.abs(figure.x - xNext) == 0 && gameField.wayToIsFreeStraight(figure.x, figure.y, xNext, yNext) then
      if gameField.getFigure(xNext, yNext).isInstanceOf[Some[Figure]] then return false
      if figure.hasBeenMoved then Math.abs(figure.y - yNext) == 1
      else Math.abs(figure.y - yNext) <= 2

    //walks diagonal -> needs to hit someone
    else if Math.abs(xNext - figure.x) == 1 && Math.abs(yNext - figure.y) == 1 then
      gameField.getFigure(xNext, yNext).isInstanceOf[Some[Figure]]
     else false
  }

  /** Verifies if a Rook is moving valid.
   *
   *  A Rook can move any number of cells straight (as long as there is no allied Piece in the way or an enemy Piece is hit).
   *  See also [[moveValidWithoutKingCheck]]
   * @param figure The to be moved Rook
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def validRookWithoutKingCheck(figure: Rook, xNext: Int, yNext: Int): Boolean = {
    gameField.wayToIsFreeStraight(figure.x, figure.y, xNext, yNext)
  }

  /** Verifies if a Knight is moving valid.
   *
   *  A Knight can move two cells horizontally then one cell vertically or two cells vertically then one horizontally.
   *
   *  It is not blocked by other Pieces, the Knight jumps to the new location.
   *
   *  See also [[moveValidWithoutKingCheck]]
   * @param figure The to be moved Knight
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def validKnightWithoutKingCheck(figure: Knight, xNext: Int, yNext: Int): Boolean = {
    if Math.abs(figure.x - xNext) == 1 && Math.abs(figure.y - yNext) == 2 ||
      (Math.abs(figure.x - xNext) == 2 && Math.abs(figure.y - yNext) == 1) then
      return true

    false
  }

  /** Verifies if a Bishop is moving valid.
   *
   *  A Bishop can move any number of cells diagonal (as long as there is no allied Piece in the way or an enemy Piece is hit).
   *
   *  See also [[moveValidWithoutKingCheck]]
   * @param figure The to be moved Bishop
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def validBishopWithoutKingCheck(figure: Bishop, xNext: Int, yNext: Int): Boolean = {
    gameField.wayToIsFreeDiagonal(figure.x, figure.y, xNext, yNext)
  }

  /** Verifies if a Queen is moving valid.
   *
   *  A Queen can move any number of cells diagonal or straight (as long as there is no allied Piece in the way or an enemy Piece is hit).
   *
   *  See also [[moveValidWithoutKingCheck]]
   * @param figure The to be moved Queen
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def validQueenWithoutKingCheck(figure: Queen, xNext: Int, yNext: Int): Boolean = {
    if figure.x == xNext || figure.y == yNext then
      gameField.wayToIsFreeStraight(figure.x, figure.y, xNext, yNext)
     else
      gameField.wayToIsFreeDiagonal(figure.x, figure.y, xNext, yNext)
  }

  /** Verifies if a King is moving valid.
   *
   *  A King can move 1 cell diagonal or straight (as long as there is no allied Piece in the way or an enemy Piece is hit).
   *
   *  See also [[moveValidWithoutKingCheck]]
   * @param figure The to be moved King
   * @param xNext x-Axis Position of the destination
   * @param yNext y-Axis Position of the destination
   * @return true if move valid, otherwise false
   */
  def validKingWithoutKingCheck(figure: King, xNext: Int, yNext: Int): Boolean = {
    val isRochade = figure.color match {
      case Color.WHITE => isShortRochadeWhite(figure, xNext, yNext) || isLongRochadeWhite(figure, xNext, yNext)
      case Color.BLACK => isShortRochadeBlack(figure, xNext, yNext) || isLongRochadeBlack(figure, xNext, yNext)
    }
    if isRochade then
      println("Rochade about to happen")
      figure.aboutToRochade = true
      return true

    Math.abs(figure.x - xNext) <= 1 && Math.abs(figure.y - yNext) <= 1
  }

  private def isRochade(xRook:Int, yRook:Int, xNow:Int, yNow:Int)(figure: King, xNext: Int, yNext: Int): Boolean = {
    if !basicRochadeChecks(figure, xRook, yRook) then return false
    xNext == xNow && yNext == yNow
  }

  private def isShortRochadeWhite(figure: King, xNext: Int, yNext: Int): Boolean = isRochade(7, 0, 6, 0)(figure, xNext, yNext)
  private def isLongRochadeWhite(figure: King, xNext: Int, yNext: Int): Boolean = isRochade(0, 0, 2, 0)(figure, xNext, yNext)
  private def isShortRochadeBlack(figure: King, xNext: Int, yNext: Int): Boolean = isRochade(0, 7, 6, 7)(figure, xNext, yNext)
  private def isLongRochadeBlack(figure: King, xNext: Int, yNext: Int): Boolean = isRochade(7, 7, 6, 7)(figure, xNext, yNext)

  private def basicRochadeChecks(figure: King, xRook: Int, yRook: Int): Boolean = {
    if figure.hasBeenMoved || !figure.firstRochade then return false
    val potentialRook = gameField.getFigure(xRook, yRook)
    if potentialRook.isEmpty then return false
    Try(potentialRook.get.asInstanceOf[Rook]) match {
      case Success(rook) => !rook.hasBeenMoved // Rook exists at that position and has not been moved yet
      case Failure(_) => false
    }
  }

}