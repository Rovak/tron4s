package tron4s.app

import akka.actor.ActorSystem
import com.google.inject.Injector

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext

case class App(injector: Injector) {

  /**
    * Shutdown the app
    */
  def shutdown()(implicit executionContext: ExecutionContext) = async {
    await(injector.getInstance(classOf[ActorSystem]).terminate())
  }
}
