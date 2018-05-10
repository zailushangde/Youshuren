package org.youshuren.endpoint

import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.youshuren.database.Database
import org.youshuren.model.Book
import org.youshuren.codec.JsonFormats._
import Implicits._
import scala.util.{Failure, Success}

object Routes {

  val Books: String = "books"
  val OnlyAvailableQueryParam: Symbol = 'only_available
  val BookNameQueryParam: Symbol = 'book_name
  val log = Logging(system, this.getClass)

  lazy val routes: Route = {

    path(Books / Remaining) { bookName =>
      headerValueByName("Authorization") { token =>
        get {
          Database[Book].getByName(bookName).unsafeRunSync match {
            case Some(book) => complete(book)
            case None       => complete(404 -> s"book not found")
          }
        } ~
        post {
          decodeRequest {
            entity(as[Book]) { book =>
              Database[Book].post(book).unsafeRunSync match {
                case Right(id) => complete(201 -> s"book successfully saved with id $id")
                case Left(err) =>
                  log.error("failed to post book {}, error {}", book, err)
                  complete(500 -> s"failed to post the book, error $err")
              }
            }
          }
        }
      }
    } ~
    path(Books) {
      get {
        headerValueByName("Authorization") { token =>
          parameter(OnlyAvailableQueryParam.as[Boolean] ? false) { onlyAvailable =>
              // TODO: query lending table to determine availability of the book
            val getBooks = Database[Book].collect(onlyAvailable)
            onComplete(getBooks.unsafeToFuture) {
              case Success(books) => complete(books)
              case Failure(error) =>
                log.error("failed to fetch books from db, error {}", error)
                complete(500 -> error)
            }
          }
        }
      }
    }
  }
}
