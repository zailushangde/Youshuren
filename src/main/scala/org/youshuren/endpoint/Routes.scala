package org.youshuren.endpoint

import org.youshuren._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.youshuren.persistence.Database
import org.youshuren.model.{Book, WeChatUser}
import org.youshuren.codec.JsonFormats._
import org.youshuren.endpoint.Routes.QueryParam._
import org.youshuren.endpoint.Routes.Path._

import scala.util.{Failure, Success}

/**
  * REST API specification for Youshuren back-end service.
  * Version : 0.1
  *
  * APIs supported in current build:
  *
  * /books/name/:bookName
  *   - GET    : fetch the book with book name given in path parameter
  *   - POST   : create a book with book name using details given in request body
  *   - PUT    : update the book with given name by replacing it with the book in the request body
  *   - DELETE : delete a book with given book name
  *
  * /books/id/:bookId
  *   - GET    : fetch the book with book id given in path parameter
  *   - POST   : create a book with book id using details given in request body
  *   - PUT    : update the book with given id by replacing it with the book in the request body
  *   - DELETE : delete a book with given book name
  *
  * /books[?only_available=false|true]
  *   - GET    : if only_available=true is given, fetch only available books, otherwise fetch all books
  *
  * /users
  *   - GET    : fetch all users
  *
  * /users/id/:userId
  *   - GET    : fetch user profile of given userId
  *   - POST   : create a new user with profile given in request body
  *   - DELETE : delete user with given id and reset owner of the user's books
  *
  * /users/id/:userId/books
  *   - GET : fetch books owned by user with given id
  */
object Routes {

  object QueryParam {
    val OnlyAvailableQueryParam: Symbol = 'only_available
    val BookNameQueryParam: Symbol = 'book_name
  }

  object Path {
    val Books: String = "books"
    val Users: String = "users"
    val Id = "id"
    val Name = "name"
    val Owner = "owner"
  }

  val log = logger(this.getClass)

  lazy val routes: Route = {
    path(Books / Id / Remaining) { bookId =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getBookById = Database[Book].getById(bookId)
          onCompleteWithBreaker(circuitBreaker)(getBookById.unsafeToFuture) {
            case Success(Right(Some(book))) => complete(200 -> book)
            case Success(Right(None))       => complete(404 -> s"book id $bookId not found")
            case Success(Left(error))       => complete(500 -> error)
            case Failure(error)             => complete(500 -> error)
          }
        } ~
        post { decodeRequest { entity(as[Book]) { book =>
          val saveBook = Database[Book].post(book)
          onCompleteWithBreaker(circuitBreaker)(saveBook.unsafeToFuture) {
            case Success(Right(res)) => complete(201 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}} ~
        put { decodeRequest { entity(as[Book]) { book =>
          val updateBook = Database[Book].put(book)
          onCompleteWithBreaker(circuitBreaker)(updateBook.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}} ~
        delete {
          val deleteBook = Database[Book].deleteById(bookId)
          onCompleteWithBreaker(circuitBreaker)(deleteBook.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    } ~
    path(Books / Name / Remaining) { bookName =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getBookByName = Database[Book].getByName(bookName)
          onCompleteWithBreaker(circuitBreaker)(getBookByName.unsafeToFuture) {
            case Success(Right(Some(book))) => complete(200 -> book)
            case Success(Right(None))       => complete(404 -> s"book not found")
            case Success(Left(error))       => complete(500 -> error)
            case Failure(error)             => complete(500 -> error)
          }
        } ~
        post { decodeRequest { entity(as[Book]) { book =>
          Database[Book].post(book).unsafeRunSync match {
            case Right(id) => complete(201 -> s"book successfully saved with id $id")
            case Left(err) =>
              log.error("failed to post book {}, error {}", book, err)
              complete(500 -> s"failed to post the book, error $err")
          }
        }}}
      }
    } ~
    path(Books) {
      get {
        headerValueByName("Authorization") { token => //TODO: implement authentication
          parameter(OnlyAvailableQueryParam.as[Boolean] ? false) { onlyAvailable =>
              // TODO: query lending table to determine availability of the book
            val getBooks = Database[Book].collect(onlyAvailable)
            onComplete(getBooks.unsafeToFuture) {
              case Success(Right(books)) => complete(200 -> books)
              case Success(Left(error))  =>
                log.error("Error in getting all the books: {}", error)
                complete(500 -> error)
              case Failure(error)        =>
                log.error("failed to fetch books from db, error {}", error)
                complete(500 -> error)
            }
          }
        }
      }
    } ~
    path(Users / Id / Remaining) { userId =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getUserById = Database[WeChatUser].getById(userId)
          onCompleteWithBreaker(circuitBreaker)(getUserById.unsafeToFuture) {
            case Success(Right(Some(user))) => complete(200 -> user)
            case Success(Right(None))       => complete(404 -> s"user id $userId not found")
            case Success(Left(err))         => complete(500 -> err)
            case Failure(err)               => complete(500 -> err)
          }
        } ~
        post { decodeRequest { entity(as[WeChatUser]) { user =>
          val saveUser = Database[WeChatUser].post(user)
          onCompleteWithBreaker(circuitBreaker)(saveUser.unsafeToFuture) {
            case Success(Right(res)) => complete(201 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}} ~
        put { decodeRequest { entity(as[WeChatUser]) { user =>
          val updateUser = Database[WeChatUser].put(user)
          onCompleteWithBreaker(circuitBreaker)(updateUser.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}} ~
        delete {
          val deleteUser = Database[WeChatUser].deleteById(userId)
          onCompleteWithBreaker(circuitBreaker)(deleteUser.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    } ~
    path(Users) {
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getAllUsers = Database[WeChatUser].collect(true)
          onCompleteWithBreaker(circuitBreaker)(getAllUsers.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    }
  }
}
