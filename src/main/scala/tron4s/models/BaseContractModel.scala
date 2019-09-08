package tron4s.models

import tron4s.infrastructure.exporter.HasDataRecord

trait BaseContractModel extends HasDataRecord {

  def contractType: Int

}
