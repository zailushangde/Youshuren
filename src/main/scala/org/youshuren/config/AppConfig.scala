package org.youshuren.config

import pureconfig.error.ConfigReaderFailures

case class AppConfig(dbConfig: DBConfig,
                     serverConfig: ServerConfig)

object AppConfig {

  lazy val config: Either[ConfigReaderFailures, AppConfig] =
    for {
      dbConfig     <- pureconfig.loadConfig[DBConfig]("db")
      serverConfig <- pureconfig.loadConfig[ServerConfig]("server")
    } yield AppConfig(dbConfig, serverConfig)
}
