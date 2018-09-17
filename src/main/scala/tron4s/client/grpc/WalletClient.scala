package tron4s.client.grpc

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.{Inject, Named, Singleton}
import org.tron.api.api.WalletGrpc.WalletStub

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class WalletClient @Inject()(
   @Named("grpc-balancer") grpcBalancer: ActorRef) {

  def full[A](request: WalletStub => Future[A]) = {
    implicit val timeout = Timeout(3.seconds)
    (grpcBalancer ? GrpcRequest(request)).mapTo[GrpcResponse].map(_.response.asInstanceOf[A])
  }

}
