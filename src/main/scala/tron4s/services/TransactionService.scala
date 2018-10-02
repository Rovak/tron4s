package tron4s.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.protobuf.ByteString
import javax.inject.Inject
import org.tron.api.api.{BytesMessage, WalletGrpc, WalletSolidityGrpc}
import org.tron.common.utils.ByteArray
import org.tron.protos.Tron.Transaction
import tron4s.client.grpc.WalletClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class TransactionService @Inject() (
  walletClient: WalletClient,
  implicit val actorSystem: ActorSystem) {

  implicit val materializer = ActorMaterializer()

  def confirmHash(hash: String)(implicit executionContext: ExecutionContext) = {

    for {
      walletSolidity <- walletClient.solidity
      transaction <- Source
        .tick(3.seconds, 3.seconds, hash)
        .mapAsync(1)(_ => walletSolidity.getTransactionById(BytesMessage(ByteString.copyFrom(ByteArray.fromHexString(hash)))))
        .filter(_.rawData.isDefined)
        .runWith(Sink.head)
    } yield transaction


  }

}
