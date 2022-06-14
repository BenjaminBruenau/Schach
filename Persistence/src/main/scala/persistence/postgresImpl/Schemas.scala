package persistence.postgresImpl

import slick.lifted.Tag
import spray.json._
import slick.jdbc.H2Profile.api.*
import slick.sql.SqlProfile.ColumnOption.SqlType

object Schemas {

  implicit val jsValueMappedColumnType: BaseColumnType[JsValue] =
    MappedColumnType.base[JsValue, String](
      { (jsonRep: JsValue) => jsonRep.prettyPrint},
      { (stringRep: String) => stringRep.parseJson},
    )

  class GameFieldTable(tag: Tag) extends Table[(Long, JsValue, JsValue, JsValue)](tag, "GAME_SAVE") {
    def id = column[Long]("SAVE_ID", SqlType("SERIAL"), O.PrimaryKey) //SqlType("SERIAL") O.AutoInc
    def gameField = column[JsValue]("GAME_FIELD")
    def currentPlayer = column[JsValue]("CURR_PLAYER")
    def gameStatus = column[JsValue]("GAME_STATUS")
    def * = (id, gameField, currentPlayer, gameStatus)
  }
  val gameFieldTable = new TableQuery(new GameFieldTable(_))

}
