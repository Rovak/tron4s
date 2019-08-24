package tron4s.grpc

import io.grpc.ManagedChannelBuilder

class GrpcFactory {

  def buildChannel(ip: String, port: Int) = {
    ManagedChannelBuilder
      .forAddress(ip, port)
      .usePlaintext(true)
      .build
  }

}
