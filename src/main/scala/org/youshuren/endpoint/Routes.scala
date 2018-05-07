package org.youshuren.endpoint

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.youshuren.database.Database
import org.youshuren.model.Book
import org.youshuren.codec.JsonFormats._

object Routes {

  val Books: String = "books"
  val OnlyAvailableQueryParam: Symbol = 'only_available
  val BookNameQueryParam: Symbol = 'book_name

  lazy val routes: Route = {

    path(Books / Remaining) { bookName =>
      headerValueByName("Authorization") { token =>
        get {
          Database[Book].getByName(bookName) match {
            case Some(book) => complete(book)
            case None       => complete(404 -> s"book not found")
          }
        } ~
        post {
          decodeRequest {
            entity(as[Book]) { book =>
              Database[Book].post(book) match {
                case Right(id) => complete(201 -> s"book successfully saved with id $id")
                case Left(err) => complete(500 -> s"failed to post the book, error $err")
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
            if(onlyAvailable) {
              // TODO: query lending table to determine availability of the book
              ???
            } else {
              ???
            }
          }
        }
      }
    }
  }
}
