package Schach.aview.gui

import Schach.controller.controllerComponent.ControllerInterface
import Schach.controller.controllerComponent.controllerBaseImpl.{ExceptionOccurred, GameFieldChanged}
import model.gameModel.figureComponent.Pawn
import persistence.RetryExceptionList

import java.awt.Color
import javax.swing.{BorderFactory, JList, JOptionPane}
import scala.swing.*
import scala.swing.event.MouseClicked

class Gui(controller: ControllerInterface) extends Frame with Reactor {

  listenTo(controller)

  reactions += {
    case _: GameFieldChanged => update()
    case ExceptionOccurred(exception) => displayError(exception)
  }

  val dimCell = new Dimension(75, 75)
  val dimLabelWest = new Dimension(50, 75)
  val dimLabelTop = new Dimension(75, 50)

  val fontItem = new Font("Arial", 0, 12)
  val fontMenu = new Font("Monospace", 2, 16)

  var fromSet = false
  var from: (Int, Int) = (-1, -1)
  var to: (Int, Int) = (-1, -1)

  listenTo(this)


  title = "Chess"
  minimumSize = new Dimension(900, 600)


  var boxPanels = Array.ofDim[Label](8, 8)

  def gridPanel = new GridPanel(8, 8) {
    for (i <- Range(0, 64)) {
      val cell = i % 8
      val row = Integer2int(7 - i / 8)

      val panel = new GridPanel(1, 1) {
        if (cell % 2 == 1)
          if (row % 2 == 1) background = Color.WHITE
          else background = Color.GRAY
        else if (row % 2 == 0) background = Color.WHITE
        else background = Color.GRAY

        val label = new Label {
          font = new Font("Monospace", 0, 50)
          preferredSize = dimCell
        }

        contents += label
        boxPanels(cell)(row) = label

        minimumSize = dimCell
        listenTo(this)
        listenTo(mouse.clicks)
        reactions += {
          case e: MouseClicked => if (!fromSet) {
            fromSet = true
            from = (cell, row)
            mouseClickSet.text = "Cell selected"
          } else {
            fromSet = false
            to = (cell, row)
            mouseClickSet.text = ""
            val move = Vector(from._1, from._2, to._1, to._2)

            controller.movePiece(move)

            controller.getGameStatus() match {
              case 1 => Dialog.showMessage(contents.head, { if (controller.getPlayer().getRed == 0) "BLACK"
                                                            else "WHITE"} + "  is checked", title = "Checked")
              case 2 => Dialog.showMessage(contents.head, { if (controller.getPlayer().getRed == 0) "BLACK"
                                                            else "WHITE"} + "  is checkmate", title = "Checkmate")
              case 3 => Dialog.showMessage(contents.head, "Invalid move", title = "Error")

              case 4 =>
                controller.changePlayer()

                var r = Dialog.showInput(contents.head, "Type in 'Queen', 'Bishop', 'Rook', 'Knight'", initial="")

                while (r.get !=  "Queen" && r.get !=  "Bishop" && r.get !=  "Rook" && r.get !=  "Knight") {
                  r = Dialog.showInput(contents.head, "Type in 'Queen', 'Bishop', 'Rook', 'Knight'", initial="")
                }

                r.get match {
                  case "Queen" => controller.convertPawn("queen")
                  case "Bishop" => controller.convertPawn("bishop")
                  case "Rook" => controller.convertPawn("rook")
                  case "Knight" => controller.convertPawn("knight")
                }

                controller.refreshStatus()
                controller.changePlayer()
                update()
              case _ =>
            }
          }
        }
      }

      contents += panel
    }
  }

  val mouseClickSet : Label = new Label(){
    font = new Font("Monospaced", 1, 20)
    horizontalAlignment = Alignment.Left
  }

  val checkedPawnWhite : Label = new Label(){
    font = new Font("Monospaced", 1, 30)
    horizontalAlignment = Alignment.Left
  }
  val checkedElseWhite : Label = new Label(){
    font = new Font("Monospaced", 1, 30)
    horizontalAlignment = Alignment.Left
  }

  val checkedPawnBlack: Label = new Label(){
    font = new Font("Monospaced", 1, 30)
    horizontalAlignment = Alignment.Left
  }
  val checkedElseBlack: Label = new Label(){
    font = new Font("Monospaced", 1, 30)
    horizontalAlignment = Alignment.Left
  }

  val playersTurn: Label = new Label() {
    horizontalAlignment = Alignment.Left
    font = new Font("Monospaced", 1, 20)
  }

  def gameInfos: GridPanel = new GridPanel(15,1) {
    border = BorderFactory.createEmptyBorder(0, 10, 0, 10)
    preferredSize = new Dimension(280, 300)
    contents += playersTurn
    contents += mouseClickSet

    contents += new Label("Graveyard") {
      horizontalAlignment = Alignment.Left
      font = new Font("Monospaced", 1, 30)
    }

    contents += new Label("White:") {
      horizontalAlignment = Alignment.Left
      font = new Font("Monospaced", 1, 20)
    }
    contents += checkedPawnWhite
    contents += checkedElseWhite
    contents += new Label("Black:") {
      horizontalAlignment = Alignment.Left
      font = new Font("Monospaced", 1, 20)
    }
    contents += checkedPawnBlack
    contents += checkedElseBlack
  }

  def labelWest: GridPanel = new GridPanel(8, 1) {
    for (i <- Range(8, 0, -1)) {
      contents += new Label {
        font = new Font("Monospace", 2, 20)
        text = i.toString
        preferredSize = dimLabelWest
      }
    }
  }

  def labelNorth: GridPanel = new GridPanel(1, 9) {

    border = BorderFactory.createEmptyBorder(0, 50, 0, 280)
    for (i <- List('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H')) {
      contents += new Label {
        font = new Font("Monospace", 2, 20)
        horizontalAlignment = Alignment.Center
        text = i.toString
        preferredSize = dimLabelTop
      }
    }
  }

  contents =
    new BorderPanel {
      add(gridPanel, BorderPanel.Position.Center)
      add(labelWest, BorderPanel.Position.West)
      add(labelNorth, BorderPanel.Position.North)
      add(gameInfos, BorderPanel.Position.East)
    }

  menuBar = new MenuBar {
    contents += new Menu("File") {
      font = fontMenu

      contents += new MenuItem(Action("New")(controller.createGameField())){
        font = fontItem
      }

      contents += new MenuItem(Action("Close")(System.exit(0))){
        font = fontItem
      }

      contents += new MenuItem(Action("Save Game")(controller.saveGame())){
        font = fontItem
      }

      contents += new MenuItem(Action("Load Game")(showGameSaves())){
        font = fontItem
      }
    }
    contents += new Menu("Edit") {
      font = fontMenu

      contents += new MenuItem(Action("Undo")(controller.undo())){
        font = fontItem
      }

      contents += new MenuItem(Action("Redo")(controller.redo())){
        font = fontItem
      }

      contents += new MenuItem(Action("Save")(controller.save())){
        font = fontItem
      }

      contents += new MenuItem(Action("Load")(
        if (controller.caretakerIsCalled()) controller.restore()
        else println("No Save created yet"))) {
        font = fontItem
      }

    }
    contents += new Menu("About") {
      font = fontMenu
    }
  }

  def showGameSaves() = {
    val saves = controller.listSaves()
    val list = saves.map(save => save._1)

    val input = JOptionPane.showInputDialog(null, "Choose Save", "Save Selection",
      JOptionPane.QUESTION_MESSAGE, null, list.toArray, list.head)
    input match
      case id: Long =>
        val selectedSave = saves.filter(save => save._1 == id).head
        val gameField = selectedSave._2
        controller.replaceGameField(gameField)
      case _ => None
  }

  visible = true

  def update(): Unit = {
    boxPanels.foreach(_.foreach(_.text = ""))
    val unchecked = controller.getGameField.filter(!_.checked)
    for (figure <- unchecked) {
      boxPanels(figure.x)(figure.y).text = figure.toString
    }

    checkedPawnBlack.text = ""
    checkedPawnWhite.text = ""
    checkedElseBlack.text = ""
    checkedElseWhite.text = ""
    val checked = controller.getGameField.filter(_.checked)
    for (figure <- checked) {
      figure match {
        case p : Pawn => if (figure.color == Color.WHITE) checkedPawnWhite.text += figure.toString
                          else checkedPawnBlack.text += figure.toString
        case _ =>   if (figure.color == Color.WHITE) checkedElseWhite.text += figure.toString
                          else checkedElseBlack.text += figure.toString
      }
    }

    playersTurn.text = "It's your turn: " + (if (controller.getPlayer().getRed == 0) "Black"
                                              else "White")
  }

  def displayError(exception: Throwable): Unit = {
    val lastError = exception.asInstanceOf[RetryExceptionList].list.last._2.getMessage
    Dialog.showMessage(contents.head, lastError, title = "Error")
  }
}