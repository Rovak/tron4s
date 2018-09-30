interp.load.cp(ammonite.ops.Path("/home/rovak/workspace/tronweb4s/target/scala-2.12/tron4s.jar"))

@

import org.tron.common.utils.ByteArray
import tron4s.dsl._
import tron4s.dsl.TransactionFactory._
import tron4s.domain._

implicit val blockchain = new PostgreSQLBlockchainDatabase("localhost", 5432, "root", "password")
//implicit val tronscanDatabase = new TronscanApiDatabase()

val votes = Query
  .votes
    .forAddress(AddressBook.Sesameseed)
    .rounds(1, 2, 3, 5)
  .execute

Exporter.toExcel(votes)

implicit val privateKey = PrivateKey(ByteArray.fromHexString("2DF8B243087D0865BE210F0AD1BEB87AE2F48AE824674197366A2328D12CFE4D"))

transfer
  .to(AddressBook.Sesameseed)
  .amount(1 trx)
  .sign

transfer
  .to(AddressBook.Sesameseed)
  .amount(1 trx)
  .sign

transferToken
  .token("SEED")
  .to(AddressBook.Sesameseed)
  .amount(1)
  .sign


val TwoTRX = 1.sun + 1.trx

println("sun = " + TwoTRX)
