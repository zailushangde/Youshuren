package org.youshuren.persistence

import org.youshuren.config.{DBConfig, PostgresConfig}

object PersistenceService {

  def start(dBConfig: DBConfig): Either[Throwable, Unit] = {
    startPostgres(dBConfig.postgres)
  }

  private[this] def startPostgres(postgresConfig: PostgresConfig): Either[Throwable, Unit] =
    Postgres.start(postgresConfig)
}
