package org.youshuren.config

case class DBConfig(postgres: PostgresConfig)

case class PostgresConfig(driver: String,
                          url: String,
                          username: String,
                          password: String,
                          tables: DBTables)

case class DBTables(wechatUsers: String,
                    wegroups: String,
                    books: String)