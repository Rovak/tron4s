package tron4s.client.grpc

import java.util.UUID

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}
import org.tron.api.api.WalletGrpc.WalletStub
import org.tron.api.api.WalletSolidityGrpc.{WalletSolidity, WalletSolidityStub}

import scala.concurrent.Future

@Singleton
class WalletClient @Inject()(
   @Named("grpc-balancer") grpcBalancer: ActorRef) {

  def full: Future[WalletStub] = ???

//  def full[A](request: WalletStub => Future[A]): Future[A] = {
//    implicit val timeout = Timeout(3.seconds)
//    (grpcBalancer ? GrpcRequest(request)).mapTo[GrpcResponse].map(_.response.asInstanceOf[A])
//  }

  def fullRequest[A](request: WalletStub => Future[A], id: UUID = UUID.randomUUID()): Future[A] = ???
  def solidity: Future[WalletSolidityStub] = ???
}
