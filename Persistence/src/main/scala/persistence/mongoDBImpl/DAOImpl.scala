package persistence.mongoDBImpl

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
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, ObservableFuture, Observer, SingleObservable, Subscription}
import persistence.{DAOInterface, FutureHandler}
import slick.dbio.DBIOAction
import spray.json.*
import spray.json.DefaultJsonProtocol.vectorFormat

import java.awt.Color
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class DAOImpl(uri: String) extends DAOInterface with GameFieldJsonProtocol with SprayJsonSupport {
  // local docker mongo: mongodb://root:schachconnoisseur@localhost:27017
  // local docker mongo from docker : mongodb://root:schachconnoisseur@mongodb:27017
  // server: mongodb+srv://schach:schach123@schach.si7w8.mongodb.net/myFirstDatabase?retryWrites=true&w=majority
  System.setProperty("org.mongodb.async.type", "netty")
  val client: MongoClient = MongoClient(uri)
  val db: MongoDatabase = client.getDatabase("schach")
  val collection: MongoCollection[Document] = db.getCollection("schach")
  val autoIncCollection: MongoCollection[Document] = db.getCollection("autoinc")

  val futureHandler: FutureHandler = new FutureHandler

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  var currentId: Long = -1

  var cachedSaves: Vector[(Long, GameField)] = Vector.empty

  val firstRecordsFuture: Future[Vector[(Long, GameField)]] = for {
    documents <- collection.find().toFuture()
    gameSaves <- convertDocumentsToGameSaves(documents)
  } yield gameSaves


  firstRecordsFuture onComplete {
    case Success(value) => cachedSaves = value
    case Failure(exception) => cachedSaves = Vector.empty
  }
  
  Await.result(firstRecordsFuture, Duration.Inf)

  collection.countDocuments().subscribe(new Observer[Long] {
    override def onSubscribe(subscription: Subscription): Unit = subscription.request(1)

    override def onNext(result: Long): Unit = currentId = result

    override def onError(e: Throwable): Unit = println("Initial Document Count Failed")

    override def onComplete(): Unit = println("Completed Initial Document Count")
  })


  override def loadGame(saveID: Long): Future[GameField] = {
    val cachedSave = cachedSaves.find(save => save._1 == saveID)
    if (cachedSave.nonEmpty) {
      return Future(cachedSave.get._2)
    }

    val loadGameFuture =
      futureHandler.resolveFutureNonBlocking(collection.find(equal("saveId", saveID)).first().head())
    for {
      document <- loadGameFuture
      gameField <- convertDocumentToGameField(document)
    } yield gameField
  }

  override def saveGame(gameField: GameField): Future[Boolean] = {
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

    collection.insertOne(doc).head().map(result => result.wasAcknowledged())
  }

  override def listSaves: Future[Vector[(Long, GameField)]] = {
    val loadSavesFuture =
      futureHandler.resolveFutureNonBlocking(collection.find().toFuture())

    for {
      documents <- loadSavesFuture
      gameSaves <- convertDocumentsToGameSaves(documents)
    } yield gameSaves
  }

  private def convertDocumentsToGameSaves(documents: Seq[Document]): Future[Vector[(Long, GameField)]] =
    Future(documents.map(document => {
      val saveId = document.get("saveId").get.asNumber().longValue()
      val field = document.get("gameField").get.asString().getValue.parseJson.convertTo[GameField]
      (saveId, field)
    }).toVector)

  private def convertDocumentToGameField(document: Document): Future[GameField] = {
    val value = document.get("gameField").get
    val field = value.asString().getValue.parseJson.convertTo[GameField]
    Future(field)
  }

}

