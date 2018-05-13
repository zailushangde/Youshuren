package org.youshuren.controllers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import org.youshuren.{Result, circuitBreaker}
import org.youshuren.model.Book
import org.youshuren.persistence.Database

import scala.util.Try

class BookController extends Directives with SprayJsonSupport {

  import org.youshuren.controllers.utils.RouteDirectiveUtils._

  private val booksPathPrefix = pathPrefix(PathParts.Api / PathParts.Books)

  val getBook: Route =
    (booksPathPrefix & get & path(Segment))(id => getBookById(id))

  private def getBookById(bookId: String): StandardRoute = {

    val res: Try[Either[Throwable, Option[Book]]] = for {
      _                                 <- validateAuthorizationHeader()
      getBookById: Result[Option[Book]] = Database[Book].getById(bookId)
      res                               <- onCompleteWithBreaker(circuitBreaker)(getBookById.unsafeToFuture)
    } yield res

    toRoute(res)
  }

  val bookRoute: Route = getBook
}
