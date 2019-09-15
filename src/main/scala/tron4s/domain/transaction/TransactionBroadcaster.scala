package tron4s.domain.transaction

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import cats.kernel.instances.hash
import com.google.protobuf.ByteString
import javax.inject.Inject
import monix.execution.Scheduler
import org.tron.api.api.{BytesMessage, WalletGrpc}
import org.tron.common.utils.ByteArray
import org.tron.protos.Tron.Transaction
import tron4s.Implicits._
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.models.{TransactionException, TransactionResult}
import tron4s.utils.FutureUtils.delay

import scala.async.Async.{async, await}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class TransactionBroadcaster @Inject()(
                                        walletClient: GrpcWalletClient,
                                        transactionVerifier: TransactionVerifier,
                                        implicit val actorSystem: ActorSystem) {

  implicit val materializer = ActorMaterializer()


  def broadcastWithRetries(wallet: WalletGrpc.Wallet, transaction: Transaction, retryStrategy: TransactionRetries)(implicit executionContext: Scheduler): Future[Either[TransactionException, TransactionResult]] = async {

    println("broadcasting..")

    await(wallet.broadcastTransaction(transaction)) match {
      case result if result.result =>
        println("success", result)
        // Broadcast was a success
        await(transactionVerifier.confirmTransaction(wallet, transaction, retryStrategy.confirmations)) match {
          case true =>
            // Broadcast and confirmations were a success
            Right(TransactionResult(transaction, result.code, result.message.decodeString))
          case false if retryStrategy.hasRetries =>
            await(delay(3.seconds))
            // Could not confirm, try again to broadcast
            await(broadcastWithRetries(wallet, transaction, retryStrategy.useRetry))
        }
      case result if !result.result && retryStrategy.hasRetries =>
        println(result)
        await(delay(3.seconds))
        // The broadcast failed but there were retries left
        await(broadcastWithRetries(wallet, transaction, retryStrategy.useRetry))
      case result if !result.result =>
        println(result)
        // Broadcast failed and there are no retries left
        Left(TransactionException(transaction, result.code, result.message.decodeString))
    }
  }
}
