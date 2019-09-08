package tron4s.models

import org.joda.time.DateTime
import tron4s.domain.Address
import tron4s.infrastructure.exporter.{Field, HasDataRecord, Record, RecordFormatter}

object TokenModel {

  implicit val tokenModelFormatter: RecordFormatter[TokenModel] = (record: TokenModel) => Record(
    Field("ownerAddress", record.ownerAddress.address),
    Field("name", record.name),
    Field("abbreviation", record.abbreviation),
    Field("totalSupply", record.totalSupply.toString),
    Field("startTime", record.startTime.toString),
    Field("endTime", record.endTime.toString),
    Field("description", record.description.trim),
    Field("url", record.url),
  )
}

case class TokenModel(
  ownerAddress: Address,
  name: String,
  abbreviation: String,
  totalSupply: Long,
  startTime: DateTime,
  endTime: DateTime,
  description: String,
  url: String) extends HasDataRecord {

  def toRecord = TokenModel.tokenModelFormatter.format(this)
}
