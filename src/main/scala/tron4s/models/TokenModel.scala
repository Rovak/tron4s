package tron4s.models

import org.joda.time.DateTime
import tron4s.domain.Address

case class TokenModel(
  ownerAddress: Address,
  name: String,
  abbreviation: String,
  totalSupply: Long,
  startTime: DateTime,
  endTime: DateTime,
  description: String,
  url: String) extends HasDataRecord {

  def toRecord = Record(
    Field("ownerAddress", ownerAddress.address),
    Field("name", name),
    Field("abbreviation", abbreviation),
    Field("totalSupply", totalSupply.toString),
    Field("startTime", startTime.toString),
    Field("endTime", endTime.toString),
    Field("description", description.trim),
    Field("url", url),
  )
}
