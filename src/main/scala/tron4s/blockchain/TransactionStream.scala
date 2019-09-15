package tron4s.blockchain

import akka.stream.scaladsl.Source
import org.tron.api.api.AccountPaginated
import org.tron.protos.Tron.Account
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.domain.Address
import tron4s.Implicits._

import scala.concurrent.ExecutionContext
import scala.util.Success

class TransactionStream(walletClient: GrpcWalletClient) {

  def streamToThis(address: Address, pageSize: Int = 100)(implicit executionContext: ExecutionContext) = {
    Source
      .single(0)
      .mapAsync(1)(_ => walletClient.solidityExtension)
      .flatMapConcat(client => {
        Source.unfoldAsync(0) { offset =>
          client.getTransactionsFromThis(AccountPaginated(Some(Account(address = address.address.decode58)), offset, pageSize)).map {
            case transactions if transactions.transaction.nonEmpty =>
              Some(offset + pageSize, transactions)
            case _ =>
              None
          }
        }
      })
      .mapConcat(_.transaction.toList)
  }

  def streamFromThis(address: Address, pageSize: Int = 100)(implicit executionContext: ExecutionContext) = {
    Source
      .single(0)
      .mapAsync(1)(_ => walletClient.solidityExtension)
      .flatMapConcat(client => {
        Source.unfoldAsync(0) { offset =>
          client.getTransactionsFromThis(AccountPaginated(Some(Account(address = address.address.decode58)), offset, pageSize)).map {
            case transactions if transactions.transaction.nonEmpty =>
              Some(offset + pageSize, transactions)
            case _ =>
              None
          }
        }
      })
      .mapConcat(_.transaction.toList)
  }


}
