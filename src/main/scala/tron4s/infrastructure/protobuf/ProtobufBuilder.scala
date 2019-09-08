package tron4s.infrastructure.protobuf

trait ProtobufBuilder[A] {
  def toProto(record: A): Any
}
