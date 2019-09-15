package tron4s.exporter

import akka.Done
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import tron4s.infrastructure.exporter.HasDataRecord

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future}

class DataExporter {

  def exportCsv(data: Source[HasDataRecord, _])(implicit actorMaterializer: ActorMaterializer, executionContext: ExecutionContext): Future[Done] = {

    var isFirst = true

    data
      .runWith(Sink.foreach { row =>
        if (isFirst) {
          // Print header for first row
          println(row.toRecord.fields.map(_.name).mkString(","))
          isFirst = false
        }
        println(row.toRecord.toCsv)
      })
  }

  def exportXml(data: Source[HasDataRecord, _])(implicit actorMaterializer: ActorMaterializer, executionContext: ExecutionContext): Future[Unit] = async {

    val p = new scala.xml.PrettyPrinter(80, 4)

    val xmlData = await(data
      .runWith(Sink.seq))

    val doc = (
      <Document>
        { xmlData.map(x => x.toRecord.toXml) }
      </Document>
      )

    println(p.format(doc))
  }

  def exportData(data: Source[HasDataRecord, _], format: String = "csv")(implicit actorMaterializer: ActorMaterializer, executionContext: ExecutionContext) = async {

    format.toLowerCase match {
      case "csv" =>
        await(exportCsv(data))

      case "xml" =>
        await(exportXml(data))

      case _ =>
        data.runWith(Sink.foreach(println))
    }
  }

}