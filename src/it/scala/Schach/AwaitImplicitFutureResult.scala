package Schach

import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField

trait AwaitImplicitFutureResult {
  val timeout = 150
  val gameField: GameField = (new ChessGameFieldBuilder).getNewGameField

  def getVectorAfterMove(moveVec: Vector[Int]): Vector[Figure] = gameField.moveTo(moveVec(0), moveVec(1), moveVec(2), moveVec(3))

  def waitUntilResult[T](functionToExecute: () => Vector[T], expectedResult: Vector[T]): Vector[T] = {
    var result: Vector[T] = Vector.empty
    Thread.sleep(timeout)
    while (!result.equals(expectedResult)) {
      result = for (res <- functionToExecute()) yield res
    }
    result
  }
  
  def awaitTimeout(timeout: Long)(request: () => Vector[Figure]): Unit = 
    Thread.sleep(timeout) 
    for {piece <- request()} yield piece
}
