package org.youshuren.endpoint

import org.youshuren._
import akka.http.scaladsl.Http
import org.youshuren.config.ServerConfig

import scala.util.{Failure, Success}

object Server {

  lazy val log = logger(this.getClass)
  private[this] var networkBinding: Http.ServerBinding = _

  def start(serverConfig: ServerConfig): Unit = {
    Http().bindAndHandle(Routes.routes, serverConfig.host, serverConfig.port) onComplete {

      case Success(binding) =>
        networkBinding = binding
        log.info("Server started at host {} port {}", serverConfig.host, serverConfig.port)

      case Failure(error)   =>
        log.error("failed to start server at host {} port {}, error: {}", serverConfig.host, serverConfig.port, error)
    }
  }
}
