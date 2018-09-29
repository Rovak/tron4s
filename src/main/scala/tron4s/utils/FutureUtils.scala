package tron4s.utils

import akka.actor.Scheduler
import akka.pattern.after

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object FutureUtils {

  def retry[T](minBackoff: FiniteDuration, maxBackoff: FiniteDuration, randomFactor: Double = 1)(f: () => Future[T])(implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    f() recoverWith {
      case _ if minBackoff < maxBackoff =>
        val nextBackof = minBackoff.toMillis * (1 + randomFactor)
        after(nextBackof.milliseconds, s)(retry(nextBackof.milliseconds, maxBackoff, randomFactor)(f))
    }
  }
}
