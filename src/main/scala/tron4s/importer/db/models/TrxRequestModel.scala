package tron4s.importer.db.models

import com.google.inject.{Inject, Singleton}
import org.joda.time.DateTime
import tron4s.importer.db.PgProfile.api._
import tron4s.importer.db.TableRepository
import play.api.db.slick.DatabaseConfigProvider

case class TrxRequestModel(
  address: String,
  ip: String,
  dateCreated: DateTime = DateTime.now)

class TrxRequestModelTable(tag: Tag) extends Table[TrxRequestModel](tag, "trx_request") {
  def address = column[String]("address", O.PrimaryKey)
  def ip = column[String]("ip")
  def dateCreated = column[DateTime]("date_created")
  def * = (address, ip, dateCreated) <> (TrxRequestModel.tupled, TrxRequestModel.unapply)
}

@Singleton()
class TrxRequestModelRepository @Inject() (val dbConfig: DatabaseConfigProvider) extends TableRepository[TrxRequestModelTable, TrxRequestModel] {

  lazy val table = TableQuery[TrxRequestModelTable]

  def findAll = run {
    table.result
  }

  def findByAddress(address: String) = run {
    table.filter(_.address === address).result.headOption
  }

  def findByRecentIp(ip: String) = run {
    val since = DateTime.now.minusHours(1)
    table.filter(x => x.dateCreated >= since && x.ip === ip).result.headOption
  }

}