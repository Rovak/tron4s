package tron4s.cli

import java.util.Calendar

import com.google.inject.Guice
import tron4s.Module
import tron4s.cli.commands.{CreateTransferCmd, CurrentRoundCmd, ScanNodesCmd, VoteRoundCmd}

object AppCli {

  def buildInjector = {
    Guice.createInjector(new Module)
  }

  def main(args: Array[String]): Unit = {

    val app = tron4s.App(buildInjector)

    val parser = new scopt.OptionParser[AppCmd]("tron4s") {
      head("TRON 4 Scala", "0.1")

      cmd("round").action((_, c) => c.copy(cmd = Some(CurrentRoundCmd())))
        .text("get current round")

      cmd("votes").action((_, c) => c.copy(cmd = Some(VoteRoundCmd())))
        .text("get votes")

      cmd("new_transaction").action((_, c) => c.copy(cmd = Some(CreateTransferCmd(app))))
        .text("build a transaction")

      cmd("scan_nodes").action((_, c) => c.copy(cmd = Some(ScanNodesCmd(app))))
        .text("scan network nodes")
    }

    parser.parse(args, AppCmd()).foreach { config =>
      config.cmd.foreach(_.execute(config))
    }
  }

}
