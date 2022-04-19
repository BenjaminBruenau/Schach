package model.gameFieldComponent

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameFieldInterfaceSpec extends AnyWordSpec with Matchers{

  "An Interface" should {

    "have some states" in {
      GameStatus.Running.value should be (0)
      GameStatus.Checked.value should be (1)
      GameStatus.Checkmate.value should be (2)
      GameStatus.MoveIllegal.value should be (3)
      GameStatus.PawnReachedEnd.value should be (4)
    }
  }
}
