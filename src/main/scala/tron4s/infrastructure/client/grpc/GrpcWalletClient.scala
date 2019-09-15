package tron4s.infrastructure.client.grpc

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util
import akka.util.Timeout
import com.typesafe.config.Config
import javax.inject.{Inject, Named, Singleton}
import org.tron.api.api.WalletExtensionGrpc.WalletExtensionStub
import org.tron.api.api.WalletGrpc.WalletStub
import org.tron.api.api.{WalletExtensionGrpc, WalletGrpc, WalletSolidityGrpc}
import tron4s.infrastructure.grpc.GrpcPool.{Channel, RequestChannel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class GrpcWalletClient @Inject()(
  config: Config,
  @Named("grpc-pool") grpcPool: ActorRef,
  @Named("grpc-balancer") grpcBalancer: ActorRef) {

  implicit val timeout = util.Timeout(3.seconds)

  val fullNodeIp = config.getString("fullnode.ip")
  val fullNodePort = config.getInt("fullnode.port")

  val solidityIp = config.getString("solidity.ip")
  val solidityPort = config.getInt("solidity.port")

  def full: Future[WalletStub] = {
    (grpcPool ? RequestChannel(fullNodeIp, fullNodePort)).mapTo[Channel].map(c => WalletGrpc.stub(c.channel))
  }

  def fullExtension: Future[WalletExtensionStub] = {
    (grpcPool ? RequestChannel(fullNodeIp, fullNodePort)).mapTo[Channel].map(c => WalletExtensionGrpc.stub(c.channel))
  }

  def fullRequest[A](request: WalletStub => Future[A]): Future[A] = {
    (grpcBalancer ? GrpcRequest(request)).mapTo[GrpcResponse].map(_.response.asInstanceOf[A])
  }

  def solidity: Future[WalletSolidityGrpc.WalletSolidityStub] = {
    (grpcPool ? RequestChannel(solidityIp, solidityPort)).mapTo[Channel].map(c => WalletSolidityGrpc.stub(c.channel))
  }

  def solidityExtension: Future[WalletExtensionStub] = {
    (grpcPool ? RequestChannel(solidityIp, solidityPort)).mapTo[Channel].map(c => WalletExtensionGrpc.stub(c.channel))
  }
}
