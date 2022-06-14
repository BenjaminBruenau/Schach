package persistence

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.RecoverMethods._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class FutureHandlerSpec extends AsyncWordSpec with Matchers {
  // needed to prevent endless loop when blocking for Future Result (ScalaTest is using a different Execution Context)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "A FutureHandler" should {
    val futureHandler = new FutureHandler

    "resolve a Future Blocking" in {
      val future = Future(1)
      val result = futureHandler.resolveFutureBlocking(future)
      result should be (1)
    }

    "resolve a Future Non Blocking" in {
      val future = Future(1)
      val resultFuture = futureHandler.resolveFutureNonBlocking(future)
      resultFuture map {
        result => result should be (1)
      }
    }

    "resolving a Future Non Blocking with Failures should make use of its retry Feature" in {
      val errorMessage = "Much Error"
      val failingFuture = Future.failed(new Throwable(errorMessage))

      val futureException = recoverToExceptionIf[RetryExceptionList] {
        futureHandler.resolveFutureNonBlocking(failingFuture)
      }
      futureException map {
        exceptionList =>
          exceptionList.list.length should be (2)
          exceptionList.list.head._2.getMessage should be (errorMessage)
      }
    }
  }

}
