package tron4s.infrastructure.exporter

import org.apache.commons.text.StringEscapeUtils

case class Field(name: String, value: String)

case class Record(fields: Field*) {

  def toCsv = fields.map(_.value).mkString(",")

  def toXml = (
    <Record>
      {fields.map(field => <Field>{StringEscapeUtils.escapeCsv(field.value)}</Field>.copy(label = field.name))}
    </Record>
    )
}

trait RecordFormatter[A] {
  def format(record: A): Record
}
