package tron4s.app.cli.commands

import tron4s.app.cli.AppCmd
import tron4s.importer.db.models.BlockModelRepository

import scala.async.Async._

case class ResetDatabaseCmd(app: tron4s.app.App) extends Command {

  override def execute(args: AppCmd) = async {

    val blockRepository = app.injector.getInstance(classOf[BlockModelRepository])

    askBoolean("Are you sure you want to reset the entire database?") match {
      case Some(true) =>
        write("Resetting database...")
        await(blockRepository.clearAll)
      case _ =>
    }

  }
}
