package org.youshuren.model

sealed trait User {
  def id: String
}

case class WeChatUser(id: String, nickName: String) extends User

case class WeGroup(id: String, nickName: String) extends User
