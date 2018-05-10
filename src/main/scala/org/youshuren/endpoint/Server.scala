package org.youshuren.endpoint

import akka.event.Logging
import akka.http.scaladsl.Http
import org.youshuren.config.AppConfig.ServerConfig._
import Implicits._

import scala.util.{Failure, Success}

object Server {

  lazy val log = Logging(system, this.getClass)
  private[this] var networkBinding: Http.ServerBinding = _

  def start: Unit = {
    Http().bindAndHandle(Routes.routes, host, port) onComplete {
      case Success(binding) =>
        networkBinding = binding
        log.info("Server started at host {} port {}", host, port)
      case Failure(error)   => log.error("failed to start server at host {} port {}, error: {}", host, port, error)
    }
  }
}
