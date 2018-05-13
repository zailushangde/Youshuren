package org.youshuren.controllers.utils

import akka.http.scaladsl.server._
import org.youshuren.model.Book

import scala.util.{Failure, Success, Try}

object RouteDirectiveUtils extends Directives {

  import org.youshuren.model.JsonFormats._

  // TODO make it generic somehow
  def toRoute(res: Try[Either[Throwable, Option[Book]]]): StandardRoute =
    res match {
      case Success(Right(Some(book))) => complete(book)
      case Success(Right(None))       => complete(404 -> "Not found")
      case Success(Left(err))         => complete(500 -> err)
      case Failure(err)               => complete(500 -> err)
    }

  def validateAuthorizationHeader(): Directive1[String] = {

    val maybeCreds = for {
      maybeCreds <- extractCredentials
      // TODO validation token
    } yield maybeCreds

    maybeCreds.flatMap {
      case None        => reject(AuthorizationFailedRejection)
      case Some(creds) => provide(creds.token())
    }
  }
}
