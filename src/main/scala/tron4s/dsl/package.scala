package tron4s

package object dsl {

  trait SunUnit {
    def conversionFactor: Double
    def value: Double
    def +(wei: SunUnit) = Sun((value * conversionFactor) + (wei.value * wei.conversionFactor))
    def -(wei: SunUnit) = Sun((value * conversionFactor) - (wei.value * wei.conversionFactor))
    def /(wei: SunUnit) = Sun((value * conversionFactor) / (wei.value * wei.conversionFactor))

    override def equals(other: Any): Boolean = other match {
      case wei: SunUnit =>
        (value * conversionFactor) == (wei.value * wei.conversionFactor)
      case _ =>
        false
    }
  }

  case class Sun(value: Double) extends SunUnit {
    def conversionFactor: Double = 1
  }

  case class TRX(value: Double) extends SunUnit {
    def conversionFactor: Double = 1e6
    def +(trx: TRX) = TRX(value + trx.value)
    def -(trx: TRX) = TRX(value - trx.value)
    def /(trx: TRX) = TRX(value / trx.value)
  }

  implicit class NumberImplicits(number: Double) {
    def trx = TRX(number)
    def sun = Sun(number)
  }

//  case class TransactionBuilder(wallet: Wallet, amount: SunUnit = Wei(0), to: Address = Address.EMPTY) {
//    def info = s"SENDING $amount FROM ${wallet.address} TO ${to.hex}"
//    def to(toAddress: Address) = copy(to = toAddress)
//  }
//
//  implicit class WalletImplicits(wallet: Wallet) {
//    def send(wei: SunUnit) = TransactionBuilder(wallet, wei)
//  }
}
