package tron4s.app.cli.commands

import tron4s.app.cli.AppCmd
import tron4s.importer.db.models.BlockModelRepository

import scala.async.Async._

case class VerifyDatabaseCmd(app: tron4s.app.App) extends Command {

  override def execute(args: AppCmd) = async {

    write("Verifying...")

  }
}
