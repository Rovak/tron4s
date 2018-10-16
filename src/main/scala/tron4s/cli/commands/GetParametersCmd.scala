package tron4s.cli.commands

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
import tron4s.cli.AppCmd
import tron4s.client.grpc.WalletClient
import tron4s.domain.PrivateKey
import tron4s.services.TransactionBuilder

import scala.async.Async._

case class GetParametersCmd(app: tron4s.App, pk: Option[String] = None, file: Option[File] = None) extends Command {

  val FIELD_TOKEN   = "token"
  val FIELD_TO      = "to"
  val FIELD_AMOUNT  = "amount"

  def execute(args: AppCmd) = async {


    println("GetParametersCmd")
  }
}
