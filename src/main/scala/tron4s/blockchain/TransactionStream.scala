package tron4s.blockchain

import akka.stream.scaladsl.Source
import org.tron.api.api.AccountPaginated
import org.tron.protos.Tron.Account
import tron4s.client.grpc.WalletClient
import tron4s.domain.Address

import scala.concurrent.ExecutionContext

class TransactionStream(walletClient: WalletClient) {

  def streamToThis(address: Address)(implicit executionContext: ExecutionContext) = {
    val pageSize = 100
    Source
      .single(0)
      .mapAsync(1)(_ => walletClient.fullExtension)
      .flatMapConcat(client => {
        Source.unfoldAsync(0) { offset =>
          client.getTransactionsToThis(AccountPaginated(Some(Account(address = address.toByteString)), offset, pageSize)).map {
            case transactions if transactions.transaction.nonEmpty =>
              Some(offset + pageSize, transactions)
            case  _ =>
              None
          }
        }
      })
      .mapConcat(_.transaction.toList)
  }

  def streamFromThis(address: Address)(implicit executionContext: ExecutionContext) = {
    val pageSize = 100
    Source
      .single(0)
      .mapAsync(1)(_ => walletClient.fullExtension)
      .flatMapConcat(client => {
        Source.unfoldAsync(0) { offset =>
          client.getTransactionsFromThis(AccountPaginated(Some(Account(address = address.toByteString)), offset, pageSize)).map {
            case transactions if transactions.transaction.nonEmpty =>
              Some(offset + pageSize, transactions)
            case  _ =>
              None
          }
        }
      })
      .mapConcat(_.transaction.toList)
  }


}
