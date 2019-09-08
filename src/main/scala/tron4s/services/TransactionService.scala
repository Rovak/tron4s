package tron4s.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.protobuf.ByteString
import javax.inject.Inject
import org.tron.api.api.BytesMessage
import org.tron.common.utils.ByteArray
import org.tron.protos.Tron.Transaction
import tron4s.infrastructure.client.grpc.WalletClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class TransactionService @Inject() (
  walletClient: WalletClient,
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
}
