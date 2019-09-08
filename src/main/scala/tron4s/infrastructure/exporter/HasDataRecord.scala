package tron4s.infrastructure.exporter

trait HasDataRecord {
  def toRecord: Record
}
