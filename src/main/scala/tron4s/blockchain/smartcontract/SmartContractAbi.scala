package tron4s.blockchain.smartcontract

import java.util

import org.tron.protos.Tron.SmartContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.AbiTypes
import org.web3j.crypto.Hash
import tron4s.Implicits._

import collection.JavaConverters._

object SmartContractAbi {

  def fromContract(smartContract: SmartContract) = {
    val abi = smartContract.abi

    SmartContractAbi(
      abi = smartContract.getAbi,
      methods = abi.map(_.entrys).getOrElse(Seq.empty).map(methodAbi => {
        SmartContractMethodAbi(
          entry = methodAbi,
          name = methodAbi.name
        )
      }).toList
    )
  }
}

case class SmartContractAbi(
  abi: SmartContract.ABI,
  methods: List[SmartContractMethodAbi]) {


  val methodsMap = methods
    .flatMap(m => m.methodIds.map(x => (x, m)))
    .toMap

}


case class SmartContractMethodAbi(
  entry: SmartContract.ABI.Entry,
  name: String) {

  val functionSelector = name + "(" + entry.inputs.map(i => i.`type`).mkString(",") + ")"
  val signature = Hash.sha3(functionSelector.toHex).slice(2, 10)

  def methodIds = Seq(
    name,
    functionSelector,
    signature
  )

//  val func = new org.web3j.abi.datatypes.Function(
//    name,
//    entry.inputs.map(e => AbiTypes.getType(e.name)).asJava,
//    util.Arrays.asList(),
//  )
//

  val inputNames = entry.inputs.map(_.name)
  val inputTypes = entry.inputs.map(_.`type`)
//  val inputInstances = func.getInputParameters.asScala.map(_.)

  def decodeInput(data: String) = {

  }
}
