package tron4s.models

import org.tron.protos.Tron.Account
import tron4s.Implicits._

object AccountModel {
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
  power: Long) extends HasDataRecord {

  def toRecord = Record(
    Field("address", address),
    Field("name", name),
    Field("balance", balance.toString),
    Field("power", power.toString),
  )
}