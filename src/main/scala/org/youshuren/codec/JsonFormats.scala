package org.youshuren.codec

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.youshuren.model.{Book, Rental, WeChatUser, WeGroup}
import spray.json.DefaultJsonProtocol

object JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val wechatUserFormat = jsonFormat4(WeChatUser)
  implicit val weGroupFormat = jsonFormat2(WeGroup)
  implicit val bookFormat = jsonFormat6(Book)
  implicit val rentalFormat = jsonFormat7(Rental.apply)
}
