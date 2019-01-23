package tron4s.facades

import javax.inject.Inject
import org.tron.protos.Tron.Account
import tron4s.client.grpc.WalletClient
import tron4s.domain.{Address, TokenBalance}
import tron4s.Implicits._

import scala.concurrent.ExecutionContext

class WalletFacade @Inject() (
  walletClient: WalletClient) {

  /**
    * Receive the TRX balance for the given address
    */
  def getBalance(address: Address)(implicit executionContext: ExecutionContext) = {
    for {
      fullNode <- walletClient.full
      account <- fullNode.getAccount(Account(address = address.address.decode58))
    } yield account.balance
  }

  /**
    * Receive the TRX balance for the given address
    */
  def getTokenBalances(address: Address)(implicit executionContext: ExecutionContext) = {
    for {
      fullNode <- walletClient.full
      account <- fullNode.getAccount(Account(address = address.address.decode58))
    } yield account.assetV2.map { case (name, amount) => TokenBalance(name, amount) }
  }

}
