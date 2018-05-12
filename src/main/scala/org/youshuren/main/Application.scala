package org.youshuren.main

import org.youshuren._
import org.youshuren.config.AppConfig
import org.youshuren.config.AppConfig.config
import org.youshuren.endpoint.Server
import org.youshuren.persistence.PersistenceService
import pureconfig.error.ConfigReaderFailures

object Application extends App {

  val log = logger(this.getClass)

  private[this] val startApp: AppConfig => Unit =
    config => for {
      _ <- PersistenceService.start(config.dbConfig)
      _ =  Server.start(config.serverConfig)
    } yield log.info("Application started")

  private[this] val onConfigError: ConfigReaderFailures => Unit =
    failures => {
      failures.toList foreach { failure =>
        log.error("Failed to start app due to configuration error {} at location {}", failure.description, failure.location)
      }
    }

  config.fold(onConfigError, startApp)
}
