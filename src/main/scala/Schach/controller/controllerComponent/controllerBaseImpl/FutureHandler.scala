package Schach.controller.controllerComponent.controllerBaseImpl

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class RetryExceptionList(list: Vector[(Int, Throwable)]) extends Exception

class FutureHandler {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val logger: Logger = LoggerFactory.getLogger(this.getClass)


  def resolveFutureBlocking[A](futureToResolve: Future[A], duration: Duration = Duration.Inf): A =
    Await.result(futureToResolve, duration)

  def resolveFutureNonBlocking[A](futureToResolve: Future[A], retries: Int = 2): Future[A] = {
    retry(retries, RetryExceptionList(Vector.empty)) {
      futureToResolve
    }
  }

  private def retry[A](numberRetries: Int, exceptionList: RetryExceptionList)(operation: => Future[A]): Future[A] = {
    if (numberRetries == 0)
      Future.failed(exceptionList)
    else
      operation transformWith {
        case Success(value) => Future(value)
        case Failure(exception) =>
          val delay = 10.milliseconds
          Thread.sleep(delay.toMillis)
          logger.info(s"Error has occurred on Request, retryNumber[$numberRetries], Retry after 10 [ms] delay")
          retry(numberRetries - 1, exceptionList.copy(exceptionList.list :+ (numberRetries, exception))) {
            operation
          }
      }
  }

  /*
  private def retryFunctional[A](numberRetries: Int)(operation: => Future[A]): Future[A] = {
    val retryIterator: Iterator[Int] = (1 to numberRetries).iterator
    val retryAttempts: Iterator[() => Future[A]] = retryIterator.map(_ => () => operation)
    var exceptionList: RetryExceptionList = RetryExceptionList(Vector.empty)
    val retryFailure = Future.failed(exceptionList).asInstanceOf[Future[A]]

    retryAttempts.foldLeft(retryFailure)((a, operation) => a recoverWith{
      case _ => operation()

    })


    retryAttempts.foldLeft(retryFailure)((future, operation) => future transformWith {
      case Success(value) => Future(value)
      case Failure(exception) =>
        exceptionList = exceptionList.copy(exceptionList.list :+ (numberRetries, exception))
        retryFunctional(numberRetries - 1){operation}
    })

    Future.unit.flatMap(_ => operation).recoverWith {
      case e if numberRetries == 0 =>
    }
  }
  */

}
