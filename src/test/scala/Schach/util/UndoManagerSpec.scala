package Schach.util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UndoManagerSpec extends AnyWordSpec with Matchers {

  "An UndoManager" should {
    val undoManager = new UndoManager

    "have a doStep, undoStep and redoStep" in {
      val command = new incrCommand
      command.state should be(0)
      undoManager.doStep(command)
      command.state should be(1)
      undoManager.undoStep()
      command.state should be(0)
      undoManager.redoStep()
      command.state should be(1)
    }

    "handle multiple steps correctly" in {
      val command = new incrCommand
      command.state should be(0)
      undoManager.doStep(command)
      command.state should be(1)
      undoManager.doStep(command)
      command.state should be(2)
      undoManager.undoStep()
      command.state should be(1)
      undoManager.undoStep()
      command.state should be(0)
      undoManager.redoStep()
      command.state should be(1)
    }
  }

}
