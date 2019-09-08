package tron4s.blockchain

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import org.tron.api.api.PaginatedMessage
import tron4s.infrastructure.client.grpc.WalletClient

import scala.concurrent.ExecutionContext

class TokenListStream(walletClient: WalletClient) {

  def buildStream(implicit executionContext: ExecutionContext) = {
    val pageSize = 100
    Source
      .single(0)
      .mapAsync(1)(_ => walletClient.full)
      .flatMapConcat(client => {
        Source.unfoldAsync(0) { offset =>

          client.getPaginatedAssetIssueList(PaginatedMessage(offset, pageSize)).map {
            case tokenList if tokenList.assetIssue.nonEmpty =>
              Some(offset + pageSize, tokenList)
            case  _ =>
              None
          }
        }
      })
      .mapConcat(_.assetIssue.toList)
      .buffer(200, OverflowStrategy.backpressure)
  }

}
