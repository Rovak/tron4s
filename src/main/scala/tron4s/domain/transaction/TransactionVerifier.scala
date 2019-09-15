package tron4s.domain.transaction

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import cats.kernel.instances.hash
import com.google.protobuf.ByteString
import javax.inject.Inject
import org.tron.api.api.{BytesMessage, WalletGrpc}
import org.tron.common.utils.ByteArray
import org.tron.protos.Tron.Transaction
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class TransactionVerifier @Inject()(
                                     walletClient: GrpcWalletClient,
                                     implicit val actorSystem: ActorSystem) {

  implicit val materializer = ActorMaterializer()

  def confirmHash(hash: String, retries: Int = 3)(implicit executionContext: ExecutionContext): Future[Transaction] = {
    for {
      transaction <- Source
        .tick(3.seconds, 3.seconds, hash)
        .take(retries)
        .mapAsync(1)(_ => walletClient.fullRequest(_.getTransactionById(BytesMessage(ByteString.copyFrom(ByteArray.fromHexString(hash))))))
        .filter(_.rawData.isDefined)
        .runWith(Sink.head)
    } yield transaction
  }

  def confirmTransaction(wallet: WalletGrpc.Wallet, transaction: Transaction, retries: Int = 3)(implicit executionContext: ExecutionContext): Future[Boolean] = {
    for {
      transaction <- Source
        .tick(3.seconds, 3.seconds, hash)
        .take(retries)
        .mapAsync(1)(_ => wallet.getTransactionInfoById(BytesMessage(ByteString.copyFrom(transaction.hashBytes))))
        .filter(_.result.isSucess)
        .runWith(Sink.head)
    } yield transaction.result.isSucess
  }
}
