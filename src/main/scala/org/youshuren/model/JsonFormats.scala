package org.youshuren.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonFormats extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val wechatUserFormat = jsonFormat2(WeChatUser)
  implicit val weGroupFormat    = jsonFormat2(WeGroup)
  implicit val bookFormat       = jsonFormat4(Book)
}
