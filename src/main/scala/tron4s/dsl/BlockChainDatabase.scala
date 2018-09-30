package tron4s.dsl

trait BlockChainStorage {

}


class PostgreSQLBlockchainDatabase(hostname: String, port: Int, username: String, password: String) extends BlockChainStorage {

}

class TronscanApiDatabase() extends BlockChainStorage {

}
