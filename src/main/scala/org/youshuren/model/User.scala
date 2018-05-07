package org.youshuren.model

sealed trait User {
  def id: String
}

case class WeChatUser(id: String, nickName: Option[String] = None) extends User

case class WeGroup(id: String, nickName: Option[String] = None) extends User
