package persistence.mongoDBImpl

import java.awt.Color
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import model.gameManager.gameManagerBaseImpl.ChessGameFieldBuilder
import model.gameModel.figureComponent.Figure
import model.gameModel.gameFieldComponent.GameFieldJsonProtocol
import model.gameModel.gameFieldComponent.gameFieldBaseImpl.GameField
import org.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.bson.collection.immutable.Document.fromSpecific
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.result.InsertOneResult
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observer, SingleObservable, Subscription}
import persistence.DAOInterface
import persistence.api.PersistenceController.{colorFormat, figureFormat, gameFieldFormat, gameStatusFormat}
import slick.dbio.DBIOAction
import spray.json.DefaultJsonProtocol.vectorFormat
import spray.json.*

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import org.mongodb.scala.ObservableFuture

import scala.util.parsing.json.JSON

class DAOImpl extends DAOInterface with GameFieldJsonProtocol with SprayJsonSupport{

  val uri: String = "mongodb://root:schachconnoisseur@mongodb:27017"
  // local docker mongo: mongodb://root:schachconnoisseur@localhost:27017
  // local dockoer mongo from docker : mongodb://root:schachconnoisseur@mongodb:27017
  // server: mongodb+srv://schach:schach123@schach.si7w8.mongodb.net/myFirstDatabase?retryWrites=true&w=majority
  System.setProperty("org.mongodb.async.type", "netty")
  val client: MongoClient = MongoClient(uri)
  val db: MongoDatabase = client.getDatabase("schach")
  val collection: MongoCollection[Document] = db.getCollection("schach")
  val autoIncCollection: MongoCollection[Document] = db.getCollection("autoinc")

  var currentId: Long = -1

  var cachedSaves: Vector[(Long, GameField)] = Vector.empty

  val firstRecordsObservable: Seq[Document] = Await.result(collection.find().toFuture(), Duration.Inf)

  firstRecordsObservable.foreach(document => {
    val saveId = document.get("saveId").get.asNumber().longValue()
    val field = document.get("gameField").get.asString().getValue.parseJson.convertTo[GameField]
    cachedSaves = cachedSaves :+ (saveId, field)
  })

  collection.countDocuments().subscribe(new Observer[Long] {
    override def onSubscribe(subscription: Subscription): Unit = subscription.request(1)

    override def onNext(result: Long): Unit = currentId = result

    override def onError(e: Throwable): Unit = println("Failed")

    override def onComplete(): Unit = println("Completed")
  })


  override def loadGame(saveID: Long): GameField = {
    val cachedSave = cachedSaves.find(save => save._1 == saveID).headOption
    if (cachedSave.nonEmpty) {
      return cachedSave.get._2
    }

    val result = Await.result(collection.find(equal("saveId", saveID)).first().head(), Duration.Inf)
    val value = result.get("gameField").get
    value.asString().getValue.parseJson.convertTo[GameField]
  }

  override def saveGame(gameField: GameField): Boolean = {
    val field = gameField.toJson.prettyPrint
    currentId += 1
    val nextId = currentId
    val doc = Document("saveId" -> nextId, "gameField" -> field)

    if (cachedSaves.length < 50) {
      cachedSaves = cachedSaves :+ (nextId, gameField)
    } else {
      //Remove cache elements in FIFO style and add them at the end
      cachedSaves = cachedSaves.patch(0, Nil, 1)
      cachedSaves = cachedSaves :+ (nextId, gameField)
    }

    collection.insertOne(doc)

    collection.countDocuments(equal("saveId", nextId)).subscribe(new Observer[Long] {
      override def onSubscribe(subscription: Subscription): Unit = subscription.request(1)

      override def onNext(result: Long): Unit = if (result == 0) documentNotFound(doc) else documentFound(doc)

      override def onError(e: Throwable): Unit = println("Failed")

      override def onComplete(): Unit = println("Completed")
    })
    true
  }

  override def listSaves: Vector[(Long, GameField)] = {
    var result: Vector[(Long, GameField)] = Vector.empty

    val observableResult = Await.result(collection.find().toFuture(), Duration.Inf)

    observableResult.foreach(document => {
      val saveId = document.get("saveId").get.asNumber().longValue()
      val field = document.get("gameField").get.asString().getValue.parseJson.convertTo[GameField]
      result = result :+ (saveId, field)
    })
    result
  }




  def documentNotFound(doc: Document) = {
    collection.insertOne(doc).subscribe(new Observer[InsertOneResult] {

      override def onSubscribe(subscription: Subscription): Unit = subscription.request(1)

      override def onNext(result: InsertOneResult): Unit = println(s"onNext $result")

      override def onError(e: Throwable): Unit = println("New Insert Failed")

      override def onComplete(): Unit = println("New Insert Complete")
    })
  }

  def documentFound(doc: Document) = {
    collection
      .findOneAndReplace(equal("saveId", currentId), doc)
      .subscribe(new Observer[Any] {

        override def onSubscribe(subscription: Subscription): Unit = subscription.request(1)

        override def onNext(result: Any): Unit = println(s"onNext $result")

        override def onError(e: Throwable): Unit = println("Failed found")

        override def onComplete(): Unit = println("Completed")
      })
  }
}

