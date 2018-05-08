package org.youshuren.config

import com.typesafe.config.ConfigFactory

object AppConfig {

  val config = ConfigFactory.load()

  object DBConfig {
    val username = config.getString("db.postgres.username")
    val password = config.getString("db.postgres.password")
    val usersTableName = config.getString("db.postgres.tables.users")
    val booksTableName = config.getString("db.postgres.tables.books")
  }

}
