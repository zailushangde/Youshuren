package org.youshuren.persistence

import org.youshuren.config.{DBConfig, PostgresConfig}

object PersistenceService {

  def start(dBConfig: DBConfig): Either[Throwable, String] = {
    startPostgres(dBConfig.postgres)
  }

  private[this] def startPostgres(postgresConfig: PostgresConfig): Either[Throwable, String] =
    Postgres.start(postgresConfig)
}
