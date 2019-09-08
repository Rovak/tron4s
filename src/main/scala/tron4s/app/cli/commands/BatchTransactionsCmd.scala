package tron4s.app.cli.commands

import java.io.File
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink}
import com.google.protobuf.any.Any
import org.tron.protos.Contract.{TransferAssetContract, TransferContract}
import org.tron.protos.Tron.Transaction
import org.tron.protos.Tron.Transaction.Contract.ContractType
import tron4s.Implicits._
import tron4s.app.cli.AppCmd
import tron4s.infrastructure.client.grpc.WalletClient
import tron4s.domain.PrivateKey
import tron4s.services.TransactionBuilder

import scala.async.Async._

case class BatchTransactionsCmd(app: tron4s.app.App, pk: Option[String] = None, file: Option[File] = None) extends Command {

  val FIELD_TOKEN   = "token"
  val FIELD_TO      = "to"
  val FIELD_AMOUNT  = "amount"

  def execute(args: AppCmd) = async {

    val client = app.injector.getInstance(classOf[WalletClient])
    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer()

    val fullWallet = await(client.full)

    val path = Paths.get(file.get.getAbsolutePath)

    val transactionBuilder = app.injector.getInstance(classOf[TransactionBuilder])

    val privateKey = PrivateKey(pk.get)
    val ownerAddress = privateKey.address

    // Retrieve block reference
    val ref = await(transactionBuilder.getBlockReference())

    val transactionStream = FileIO.fromPath(path)
      .via(CsvParsing.lineScanner())
      .via(CsvToMap.withHeadersAsStrings(java.nio.charset.Charset.defaultCharset(), FIELD_TO, FIELD_AMOUNT, FIELD_TOKEN, "data"))
      .map { csvLine =>

        csvLine(FIELD_TOKEN) match {
          case "TRX" =>
            val transferContract = TransferContract(
              ownerAddress = ownerAddress.toByteString,
              toAddress = csvLine(FIELD_TO).toByteString,
              amount = csvLine(FIELD_AMOUNT).toLong,
            )

            val contract = Transaction.Contract(
              `type` = ContractType.TransferContract,
              parameter = Some(Any.pack(transferContract.asInstanceOf[TransferContract])))

            transactionBuilder.buildTransactionWithContract(contract)

          case token =>
            val transferContract = TransferAssetContract(
              ownerAddress = ownerAddress.toByteString,
              toAddress = csvLine(FIELD_TO).toByteString,
              amount = csvLine(FIELD_AMOUNT).toLong,
              assetName = token.toByteString,
            )

            val contract = Transaction.Contract(
              `type` = ContractType.TransferAssetContract,
              parameter = Some(Any.pack(transferContract.asInstanceOf[TransferAssetContract])))

            transactionBuilder.buildTransactionWithContract(contract)
        }
      }

      // Set block references for transaction
      .map(ref.setReference)

      // Sign transactions
      .map(t => transactionBuilder.sign(t, privateKey))

      // Send transactions to network
      .alsoTo(
        Flow[Transaction]
          .mapAsync(1)(t => fullWallet.broadcastTransaction(t))
          .toMat(Sink.ignore)(Keep.right)
      )


    val transactions = await(
      transactionStream.runWith(Sink.seq)
    )

    println("TRANSACTIONS", transactions)
  }
}
