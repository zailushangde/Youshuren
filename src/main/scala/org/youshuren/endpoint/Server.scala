package org.youshuren.endpoint

import org.youshuren._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import org.youshuren.config.ServerConfig
import org.youshuren.controllers.BookController

import scala.util.{Failure, Success}

object Server {

  lazy val log = logger(this.getClass)
  private[this] var networkBinding: Http.ServerBinding = _

  val bookController = new BookController
//  val userController = new UserController

  val routes: Route = bookController.bookRoute

  def start(serverConfig: ServerConfig): Unit = {
    Http().bindAndHandle(routes, serverConfig.host, serverConfig.port) onComplete {

      case Success(binding) =>
        networkBinding = binding
        log.info("Server started at host {} port {}", serverConfig.host, serverConfig.port)

      case Failure(error)   =>
        log.error("failed to start server at host {} port {}, error: {}", serverConfig.host, serverConfig.port, error)
    }
  }
}
