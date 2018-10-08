package tron4s.models

import org.apache.commons.text.StringEscapeUtils

trait HasDataRecord {

  case class Record(fields: Field*)
  case class Field(name: String, value: String)

  def toRecord: Record

  def toCsv = toRecord.fields.map(_.value).mkString(",")

  def toXml = (
    <Record>
      {toRecord.fields.map(field => <Field>{StringEscapeUtils.escapeCsv(field.value)}</Field>.copy(label = field.name))}
    </Record>
  )

}
