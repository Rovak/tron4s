package tron4s

import akka.actor.ActorSystem
import com.google.inject.Injector

import scala.async.Async._
import scala.concurrent.ExecutionContext

case class App(injector: Injector) {

  /**
    * Shutdown the app
    */
  def shutdown()(implicit executionContext: ExecutionContext) = async {
    await(injector.getInstance(classOf[ActorSystem]).terminate())
  }
}
