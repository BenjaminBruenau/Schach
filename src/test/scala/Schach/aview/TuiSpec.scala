package Schach.aview

import Schach.GameFieldModule
import Schach.controller.controllerComponent.ControllerInterface
import com.google.inject.Guice
import model.gameManager.ChessGameFieldBuilderInterface
import model.gameModel.figureComponent
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import persistence.RetryExceptionList


class TuiSpec extends AnyWordSpec with Matchers {

  "A Tui" should {
    val injector = Guice.createInjector(new GameFieldModule)
    val controller = injector.getInstance(classOf[ControllerInterface])
    val tui = new Tui(controller)
    val input = "A1 F2"

    "convert a letter into a number for the GameField access" in {
      tui.getPoint(input.charAt(0)) should be(0)
      tui.getPoint(input.charAt(1)) should be (0)
      tui.getPoint(input.charAt(3)) should be(5)
      tui.getPoint(input.charAt(4)) should be (1)
      tui.getPoint('B') should be(1)
      tui.getPoint('C') should be(2)
      tui.getPoint('D') should be(3)
      tui.getPoint('E') should be(4)
      tui.getPoint('G') should be(6)
      tui.getPoint('H') should be(7)
      tui.getPoint('3') should be(2)
      tui.getPoint('4') should be(3)
      tui.getPoint('5') should be(4)
      tui.getPoint('6') should be(5)
      tui.getPoint('7') should be(6)
      tui.getPoint('8') should be(7)
      tui.getPoint('X') should be(-1)
    }

    "control the input via regex" in {
      controller.controlInput("A1") should be(true)
      controller.controlInput("abc def") should be(false)
      controller.controlInput("1A 2B") should be(false)
      controller.controlInput("a1 a2") should be(false)
    }

    "read input for the movePiece method via getPoint()" in {
      val read = tui.readInput(input)
      read(0) should be(0)
      read(1) should be(0)
      read(2) should be(5)
      read(3) should be(1)
    }

    "generate an error Message correctly" in {
      val errorMessage = "Fatal Error"
      val error = RetryExceptionList(Vector((1, new Throwable(errorMessage))))
      tui.printError(error)

      tui.lastOutput.contains("ERROR OCCURED") should be (true)
      tui.lastOutput.contains(errorMessage) should be (true)
    }

  }
}