package org.youshuren.codec

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.youshuren.model.{Book, WeChatUser, WeGroup}
import spray.json.DefaultJsonProtocol

object JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val wechatUserFormat = jsonFormat2(WeChatUser)
  implicit val weGroupFormat = jsonFormat2(WeGroup)
  implicit val bookFormat = jsonFormat3(Book)
}
