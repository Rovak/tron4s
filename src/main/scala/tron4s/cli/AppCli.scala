package tron4s.cli

import com.google.inject.Guice
import tron4s.Implicits._
import tron4s.Module
import tron4s.cli.commands._
import scala.concurrent.ExecutionContext.Implicits.global

object AppCli {

  def buildInjector = {
    Guice.createInjector(new Module)
  }

  def main(args: Array[String]): Unit = {

    val app = tron4s.App(buildInjector)

    val parser = new scopt.OptionParser[AppCmd]("tron4s") {

      head("TRON 4 Scala", "0.1")

      cmd("vote_round")
        .action((_, c) => c.copy(cmd = Some(CurrentRoundCmd())))
        .text("get current round")

      cmd("votes")
        .action((_, c) => c.copy(cmd = Some(VoteRoundCmd())))
        .text("get votes")

      cmd("send_trx")
        .action((_, c) => c.copy(cmd = Some(CreateTransferCmd(app))))
        .text("Send a transaction")

      cmd("scan_nodes")
        .action((_, c) => c.copy(cmd = Some(ScanNodesCmd(app))))
        .text("Scan network nodes")

      cmd("sync")
        .action((_, c) => c.copy(cmd = Some(ImportCmd(app))))
        .text("Synchronize Blockchain")
        .children(
          cmd("verify")
            .text("Verifies if the database is complete")
            .action((_, c) => c.copy(cmd = Some(VerifyDatabaseCmd(app))))
        )

      cmd("database")
        .text("Database actions")
        .children(
          cmd("reset")
            .text("Resets the database")
            .action((_, c) => c.copy(cmd = Some(ResetDatabaseCmd(app))))
        )

      cmd("tokens")
        .text("Tokens actions")
        .children(
          cmd("list")
            .text("List all tokens")
            .action((_, c) => c.copy(cmd = Some(ListTokensCmd(app))))
            .children(
              opt[String]("format")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ListTokensCmd].copy(format = x))))
                .text("set export format"),
            )
        )

      cmd("transactions")
        .text("Transaction actions")
        .children(
          cmd("from")
            .text("List transactions for specific address")
            .action((_, c) => c.copy(cmd = Some(ListTransactionsCmd(app))))
            .children(
              arg[String]("<address>")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ListTransactionsCmd].copy(fromAddress = Some(x))))),
              opt[String]("format")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ListTransactionsCmd].copy(format = x))))
                .text("set export format"),
            ),
          cmd("to")
            .text("List transactions to specific address")
            .action((_, c) => c.copy(cmd = Some(ListTransactionsCmd(app))))
            .children(
              arg[String]("<address>")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ListTransactionsCmd].copy(toAddress = Some(x))))),
              opt[String]("format")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ListTransactionsCmd].copy(format = x))))
                .text("set export format"),
            ),
        )

      cmd("tail")
        .text("monitor transactions")
        .children(
          cmd("blocks")
            .text("monitor incoming blocks")
            .action((_, c) => c.copy(cmd = Some(TailBlocksCmd(app))))
            .children(
              opt[String]("producer")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[TailBlocksCmd].copy(producer = Some(x)))))
                .text("set export format"),
            ),

          cmd("transactions")
            .text("monitor incoming transactions")
            .action((_, c) => c.copy(cmd = Some(TailTransactionsCmd(app))))
            .children(
              opt[Seq[String]]("address")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[TailTransactionsCmd].copy(address = Some(x)))))
                .text("filter by address"),
              opt[String]("token")
                .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[TailTransactionsCmd].copy(token = Some(x)))))
                .text("filter by token"),
            ),
        )


      cmd("account")
        .text("show account")
        .action((_, c) => c.copy(cmd = Some(ShowAccountCmd(app, account = ""))))
        .children(
          arg[String]("<address>")
            .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ShowAccountCmd].copy(account = x)))),
          opt[String]("node")
            .action((x, c) => c.copy(cmd = c.cmd.map(_.asInstanceOf[ShowAccountCmd].copy(node = x))))
            .text("full or solidity"),
        )
    }

    for {
      config <- parser.parse(args, AppCmd())
      cmd <- config.cmd
    } {
      runSync(cmd.execute(config))
    }

    app.shutdown()
  }

}
