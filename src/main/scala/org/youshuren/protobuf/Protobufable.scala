package org.youshuren.protobuf

trait Protobufable[A] {

  def toPB: A => Array[Byte]
  def fromPB: Array[Byte] => A
}

object Protobufable {


}