package tron4s.grpc

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.{Inject, Named, Singleton}
import tron4s.grpc.GrpcPool.{Channel, RequestChannel}

import scala.concurrent.duration._

@Singleton
class GrpcService @Inject() (
  @Named("grpc-pool") grpcPool: ActorRef) {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(3.seconds)

  def getChannel(ip: String, port: Int) = {
    (grpcPool ? RequestChannel(ip, port)).mapTo[Channel].map(_.channel)
  }
}
