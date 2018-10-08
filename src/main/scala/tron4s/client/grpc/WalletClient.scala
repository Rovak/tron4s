package tron4s.client.grpc

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util
import akka.util.Timeout
import com.typesafe.config.Config
import javax.inject.{Inject, Named, Singleton}
import org.tron.api.api.WalletExtensionGrpc.WalletExtensionStub
import org.tron.api.api.WalletGrpc.WalletStub
import org.tron.api.api.{WalletExtensionGrpc, WalletGrpc, WalletSolidityGrpc}
import tron4s.grpc.GrpcPool.{Channel, RequestChannel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class WalletClient @Inject()(
  config: Config,
  @Named("grpc-pool") grpcPool: ActorRef,
  @Named("grpc-balancer") grpcBalancer: ActorRef) {

  def full: Future[WalletStub] = {
    implicit val timeout = util.Timeout(3.seconds)
    val ip = config.getString("fullnode.ip")
    val port = config.getInt("fullnode.port")
    (grpcPool ? RequestChannel(ip, port)).mapTo[Channel].map(c => WalletGrpc.stub(c.channel))
  }

  def fullExtension: Future[WalletExtensionStub] = {
    implicit val timeout = util.Timeout(3.seconds)
    val ip = config.getString("fullnode.ip")
    val port = config.getInt("fullnode.port")
    (grpcPool ? RequestChannel(ip, port)).mapTo[Channel].map(c => WalletExtensionGrpc.stub(c.channel))
  }

  def fullRequest[A](request: WalletStub => Future[A]) = {
    implicit val timeout = Timeout(3.seconds)
    (grpcBalancer ? GrpcRequest(request)).mapTo[GrpcResponse].map(_.response.asInstanceOf[A])
  }

  def solidity = {
    implicit val timeout = util.Timeout(3.seconds)
    val ip = config.getString("solidity.ip")
    val port = config.getInt("solidity.port")
    (grpcPool ? RequestChannel(ip, port)).mapTo[Channel].map(c => WalletSolidityGrpc.stub(c.channel))
  }
}
