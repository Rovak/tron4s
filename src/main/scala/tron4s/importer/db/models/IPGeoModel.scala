package tron4s.importer.db.models

import com.google.inject.{Inject, Singleton}
import tron4s.importer.db.PgProfile.api._
import tron4s.importer.db.TableRepository
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json

case class IPGeoModel(
  ip: String,
  country: String,
  city: String,
  lat: Double,
  lng: Double,
)

class IPGeoModelTable(tag: Tag) extends Table[IPGeoModel](tag, "ip_geo") {
  def ip = column[String]("ip")
  def country = column[String]("country")  
  def city = column[String]("city")  
  def lat = column[Double]("lat")  
  def lng = column[Double]("lng")
  def * = (ip, country, city, lat, lng) <> (IPGeoModel.tupled, IPGeoModel.unapply)
}

@Singleton()
class IPGeoModelRepository @Inject() (val dbConfig: DatabaseConfigProvider) extends TableRepository[IPGeoModelTable, IPGeoModel] {

  lazy val table = TableQuery[IPGeoModelTable]

  def findAll = run {
    table.result
  }

  def findByIp(ip: String) = run {
    table.filter(_.ip === ip).result.headOption
  }

}