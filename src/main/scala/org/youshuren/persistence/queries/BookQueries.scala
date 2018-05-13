package org.youshuren.persistence.queries

import doobie.postgres.implicits._
import doobie.implicits._
import doobie.util.query.Query0
import org.youshuren.model.WeChatUser

object BookQueries {

  private val `SELECT * FROM wechatusers` = fr"""SELECT * FROM wechatusers"""

  def getUserById(id: String): Query0[WeChatUser] = {
    (
      `SELECT * FROM wechatusers` ++
        fr"""
          WHERE id = $id
        """
    ).query[WeChatUser]
  }
}
