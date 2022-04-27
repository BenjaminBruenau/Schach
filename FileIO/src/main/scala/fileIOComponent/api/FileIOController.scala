package fileIOComponent.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.typesafe.config.{Config, ConfigFactory}
import fileIOComponent.*

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object FileIOController {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()

    val host: String = config.getString("http.host")
    val port: String = config.getString("http.port")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val jsonFileIO = new fileIOJSONImpl.FileIO
    val xmlFileIO = new fileIOXMLImpl.FileIO
    

    val route =
      concat(
        //JSON
        path("fileIO" / "JSON" / "save") {
          put {
            entity(as[String]) {
              gameField =>
                jsonFileIO.saveGameJSON(gameField)
                Try(jsonFileIO.loadGameJson()) match {
                  case Success(field) => complete(HttpEntity(ContentTypes.`application/json`, field))
                  case Failure(exception) => complete(StatusCodes.BadRequest, "Invalid JSON GameField provided")
                }
            }
          }
        },
        path("fileIO" / "JSON" / "load") {
          get {
            Try(jsonFileIO.loadGameJson()) match {
              case Success(field) => complete(HttpEntity(ContentTypes.`application/json`, field))
              case Failure(exception) => complete(StatusCodes.BadRequest, "Invalid JSON GameField")
            }

          }
        },
        //XML
        path("fileIO" / "XML" / "save") {
          put {
            entity(as[String]) {
              gameField =>
                xmlFileIO.saveGameXML(gameField)
                Try(xmlFileIO.loadGameXML) match {
                  case Success(field) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, field))
                  case Failure(exception) => complete(StatusCodes.BadRequest, "Invalid XML GameField provided")
                }
            }
          }
        },
        path("fileIO" / "XML" / "load") {
          get {
            Try(xmlFileIO.loadGameXML) match {
              case Success(field) => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, field))
              case Failure(exception) => complete(StatusCodes.BadRequest, "Invalid XML GameField")
            }
          }
        },
      )


    println("Server for FileIO started at http://" + host + ":" + port + "\n Press RETURN to stop...")

    val bindingFuture = Http().newServerAt(host, 8081).bind(route)
  }


}