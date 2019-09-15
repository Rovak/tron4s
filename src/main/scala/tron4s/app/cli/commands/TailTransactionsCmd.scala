package tron4s.app.cli.commands

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import play.api.Logger
import tron4s.app.cli.AppCmd
import tron4s.infrastructure.client.grpc.GrpcWalletClient
import tron4s.importer.BlockChainStreamBuilder
import tron4s.utils.ModelUtils
import tron4s.Implicits._
import tron4s.blockchain.smartcontract.TriggerSmartContractParser
import tron4s.models.{BaseContractModel, SmartContractTriggerModel, TransferContractModel}

import scala.async.Async.{async, await}

case class TailTransactionsCmd(
  app: tron4s.app.App,
  address: Option[Seq[String]] = None,
  token: Option[String] = None,
  contractType: Option[Int] = None)  extends Command {

  override def execute(args: AppCmd) = async {

    val decider: Supervision.Decider = { exc =>
      Logger.error("SYNC NODE ERROR", exc)
      Supervision.Restart
    }

    val blockChainStreamBuilder = app.injector.getInstance(classOf[BlockChainStreamBuilder])
    val wallet = app.injector.getInstance(classOf[GrpcWalletClient])
    implicit val system = app.injector.getInstance(classOf[ActorSystem])
    implicit val materializer = ActorMaterializer(
      ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)


    val fullWallet = await(wallet.full)

    val parser = new TriggerSmartContractParser(fullWallet)


    //    val dataExporter = new DataExporter

    var stream =  blockChainStreamBuilder
      .readFullNodeBlocksContinously(fullWallet)
      .mapConcat(_.transactions.toList)

    // Filter by address
    address.foreach { from =>
      stream = stream.filter(_.getRawData.contract.head.addresses.exists(x => from.contains(x)))
    }
    // Filter by address
    contractType.foreach { contractTypeId =>
      stream = stream.filter(_.getRawData.contract.head.`type`.value == contractTypeId)
    }

    var stream2: Source[BaseContractModel, Cancellable] = stream
      .map(ModelUtils.contractModelFromProto).filter(_.isDefined).map(_.get)

    // Filter by token
    token.foreach { t =>
      stream2 = stream2.filter {
//        case x: TransferContractModel if x.token == t =>
//          true
        case _: SmartContractTriggerModel =>
          true
        case _ =>
          false
      }
    }

    await {
      stream2
        .runWith(Sink.foreach[BaseContractModel] {
          case SmartContractTriggerModel(from, contractAddress, data) =>
            parser.decodeInput(data, contractAddress).map { func =>
              println(s"${from.address}, ${contractAddress.address}: ${func}")
            }
          case transaction =>
            println("transaction", transaction.contractType, transaction.toRecord.toCsv)
        })
    }
  }
}
