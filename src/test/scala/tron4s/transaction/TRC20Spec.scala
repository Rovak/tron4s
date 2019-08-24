//package tron4s.transaction
//
//import java.math.BigInteger
//import java.util
//
//import com.google.protobuf.ByteString
//import org.specs2.mutable._
//import org.tron.api.api.{EmptyMessage, WalletGrpc}
//import org.tron.common.utils.ByteArray
//import org.tron.protos.Contract.TriggerSmartContract
//import org.web3j.abi.datatypes.Type
//import org.web3j.abi.{FunctionEncoder, FunctionReturnDecoder, TypeReference}
//import org.web3j.abi.datatypes.generated.{Int256, Uint256}
//import tron4s.domain.Address
//import tron4s.grpc.GrpcFactory
//import tron4s.Implicits._
//
//object TRC20Spec extends Specification {
//
//  val grpcFactory = new GrpcFactory
//
//  "TRC20" should {
//
//    "transfer" in {
//
//      val to = Address("TYwPjRcGEmFFYkX8yoSPgQVra69qGP969W")
//
//      val encodedFunction = FunctionEncoder.encode(
//        new org.web3j.abi.datatypes.Function(
//          "transfer",
//          util.Arrays.asList(
//            new org.web3j.abi.datatypes.Address("0x" + to.toHex.substring(2)),
//            new Uint256(BigInteger.valueOf(5000000)),
//          ),
//          util.Arrays.asList()
//        )
//      )
//
//      encodedFunction.substring(2) must equalTo("a9059cbb000000000000000000000000fbf304b7f1dbe64e4c621dcc801daf2994bf923500000000000000000000000000000000000000000000000000000000004c4b40")
//
//    }
//
//    "balanceOf" in {
//
//      val contractAddress = Address("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
//      val to = Address("TKUkwWFmiyigmqFqLy4zVmXXbikqKeJNkj")
//
//      val encodedFunction = FunctionEncoder.encode(
//        new org.web3j.abi.datatypes.Function(
//          "balanceOf",
//          util.Arrays.asList(
//            new org.web3j.abi.datatypes.Address("0x" + to.toHex.substring(2)),
//          ),
//          util.Arrays.asList()
//        )
//      )
//
//      encodedFunction.substring(2) must equalTo("70a08231000000000000000000000000684fdb264c9c65cdac2a7ef0f8b902eadfb4d8d1")
//
//    }
//
//
//    "call balanceOf" in {
//
//      val channel = grpcFactory.buildChannel("54.236.37.243", 50051)
//      val wallet = WalletGrpc.blockingStub(channel)
//
//      val contractAddress = Address("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
//      val to = Address("TNCmcTdyrYKMtmE1KU2itzeCX76jGm5Not")
//
//      val balanceOfFunc = new org.web3j.abi.datatypes.Function(
//        "balanceOf",
//        util.Arrays.asList(
//          new org.web3j.abi.datatypes.Address("0x" + to.toHex.substring(2)),
//        ),
//        util.Arrays.asList(
//          new TypeReference[org.web3j.abi.datatypes.Uint]() {}
//        )
//      )
//
//      val data = FunctionEncoder.encode(
//        balanceOfFunc
//      )
//
//      println("parameter", data)
//
//      val triggerResult = wallet.triggerConstantContract(TriggerSmartContract(
//        ownerAddress = to.toByteString,
//        contractAddress = contractAddress.toByteString,
//        data = ByteString.copyFrom(ByteArray.fromHexString(data)),
//      ))
//
//      val resultValue = FunctionReturnDecoder.decode(
//        triggerResult.constantResult.head.toHex,
//        balanceOfFunc.getOutputParameters,
//      )
//
//      println(Math.pow(10, 6))
//
//      println("resultValue", resultValue.get(0).getValue.asInstanceOf[BigInteger].divide(BigInteger.valueOf(Math.pow(10, 6).toLong)))
//
//      resultValue.get(0).getValue must equalTo(54220295965021L)
//
//    }
//  }
//}
//