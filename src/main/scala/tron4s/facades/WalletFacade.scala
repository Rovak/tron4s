package tron4s.facades

import java.math.BigInteger
import java.util

import com.google.protobuf.ByteString
import javax.inject.Inject
import org.tron.common.utils.ByteArray
import org.tron.protos.Contract.TriggerSmartContract
import org.tron.protos.Tron.Account
import org.web3j.abi.{FunctionEncoder, FunctionReturnDecoder, TypeReference}
import tron4s.Implicits._
import tron4s.client.grpc.WalletClient
import tron4s.domain.{Address, TokenBalance}

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

  /**
    * Retrieve TRC20 token balance for the given wallet and contract
    */
  def getTRC20Balance(walletAddress: Address, contractAddress: Address, decimals: Int = 0)(implicit executionContext: ExecutionContext) = {

    val balanceOfFunc = new org.web3j.abi.datatypes.Function(
      "balanceOf",
      util.Arrays.asList(
        new org.web3j.abi.datatypes.Address("0x" + walletAddress.toHex.substring(2)),
      ),
      util.Arrays.asList(
        new TypeReference[org.web3j.abi.datatypes.Uint]() {}
      )
    )

    val data = FunctionEncoder.encode(
      balanceOfFunc
    )

    for {
      fullNode <- walletClient.full
      contractResult <- fullNode.triggerConstantContract(TriggerSmartContract(
        ownerAddress = walletAddress.toByteString,
        contractAddress = contractAddress.toByteString,
        data = ByteString.copyFrom(ByteArray.fromHexString(data)),
      ))
      resultValue = FunctionReturnDecoder.decode(
        contractResult.constantResult.head.toHex,
        balanceOfFunc.getOutputParameters,
      )
    } yield resultValue.get(0).getValue
      .asInstanceOf[BigInteger]
      .divide(BigInteger.valueOf(Math.pow(10, 6).toLong))
  }
}
