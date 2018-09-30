package tron4s.dsl

import tron4s.domain.{Address, PrivateKey}


object TransactionFactory {

  case class TransferFactory(toAddress: Address = Address.EMPTY, amount: SunUnit = Sun(0)) {
    def to(address: Address) = copy(toAddress = address)
    def amount(amount: SunUnit) = copy(amount = amount)
    def sign(implicit privateKey: PrivateKey) = copy()
  }

  case class TransferTokenFactory(toAddress: Address = Address.EMPTY, token: String = "", amount: Long = 0) {
    def to(address: Address) = copy(toAddress = address)
    def amount(amount: Long): TransferTokenFactory = copy(amount = amount)
    def token(token: String) = copy(token = token)
    def sign(implicit privateKey: PrivateKey) = copy()
  }

  def transfer = TransferFactory()
  def transferToken = TransferTokenFactory()

}
