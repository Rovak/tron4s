package tron4s.models

import org.tron.protos.Tron.Account
import tron4s.Implicits._
import tron4s.infrastructure.exporter._

object AccountModel {

  implicit val recordFormatter = new RecordFormatter[AccountModel] {
    override def format(record: AccountModel): Record = {
      Record(
        Field("address", record.address),
        Field("name", record.name),
        Field("balance", record.balance.toString),
        Field("power", record.power.toString),
      )
    }
  }

  def fromProto(account: Account) = {
    AccountModel(
      address = account.address.encode58,
      name = account.accountName.decodeString,
      balance = account.balance,
      power = account.frozen.map(_.frozenBalance).sum,
    )
  }
}


case class AccountModel(
  address: String,
  name: String,
  balance: Long,
  power: Long)