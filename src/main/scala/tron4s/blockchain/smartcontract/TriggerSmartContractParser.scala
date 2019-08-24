package tron4s.blockchain.smartcontract

import ch.qos.logback.core.encoder.ByteArrayUtil
import org.tron.api.api.BytesMessage
import org.tron.api.api.WalletGrpc.WalletStub
import tron4s.domain.Address

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class TriggerSmartContractParser(wallet: WalletStub) {

  var contractCache = Map[String, Future[SmartContractAbi]]()

  def getContract(address: Address) = {
    contractCache.getOrElse(address.address, {
      contractCache = contractCache + (
        address.address -> wallet
          .getContract(BytesMessage(address.toByteString))
          .map(SmartContractAbi.fromContract)
        )

      contractCache(address.address)
    })
  }

  /**
    * Decodes the given input
    */
  def decodeInput(data: Array[Byte], contractAddress: Address) = async {
    val contract = await(getContract(contractAddress))

    val (methodName, inputData) = ByteArrayUtil.toHexString(data).replace("0x", "").splitAt(8)

//    println("methods: " + contract.methodsMap.keys.mkString(","))
//    println("method 1: " + methodName)

    FunctionCall(
      contract.methodsMap.get(methodName).map(_.name).getOrElse("unknown")
    )
  }

}
