package org.youshuren.endpoint

import org.youshuren._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.youshuren.persistence.{BookPredicate, Database, Predicate}
import org.youshuren.model.{Book, Rental, WeChatUser}
import org.youshuren.codec.JsonFormats._
import org.youshuren.endpoint.Routes.Path._

import scala.util.matching.Regex
import scala.util.{Failure, Success}
import scala.language.postfixOps

/**
  * REST API specification for Youshuren back-end service.
  * Version : 0.1
  *
  * APIs supported in current build:
  *
  * /books/id/:bookId
  *   - GET    : fetch the book with book id given in path parameter
  *   - PUT    : update the book with given id by replacing it with the book in the request body
  *   - DELETE : delete a book with given book name
  *
  * /books/tag/:tag[?only_available=false|true]
  *   - GET    : fetch all the books that contain the given tag
  *
  * /books[?only_available=false|true]
  *   - GET    : if only_available=true is given, fetch only available books, otherwise fetch all books
  *   - POST   : create a book with book name using details given in request body
  *
  * /users
  *   - GET    : fetch all users
  *   - POST   : create a new user with details given in request body
  *
  * /users/id/:userId
  *   - GET    : fetch user profile of given userId
  *   - PUT    : update the user at given Id
  *   - DELETE : delete user with given id and reset owner of the user's books
  *
  * /users/id/:userId/books
  *   - GET    : fetch books owned by user with given id
  *
  * /users/id/:userId/books/tag/:tag
  *   - GET    : fetch books owned by user with given id and tagged by given tag
  *
  * /rentals/
  *   - GET    : fetch all the rentals
  *   - POST   : create a new rental
  *
  * /rentals/id/:rentalId
  *   - GET    : fetch a specific rental at given id
  *
  * /rentals/id/:rentalId/[approve|reject]
  *   - PATCH  : update status of the given rental to either approved or rejected
  *
  * /rentals/status/:status
  *   - GET    : fetch all the rentals having given status
  *
  * /rentals/owner/:ownerId
  *   - GET    : fetch all the rentals where the given userId is owner
  *
  * /rentals/borrower/:borrowerId
  *   - GET    : fetch all the rentals where the given userId is owner
  *
  * /rentals/book/:bookId
  *   - GET    : fetch all the rentals on the given book
  */
object Routes {

  object QueryParam {
    val OnlyAvailableQueryParam: Symbol = 'only_available
    val BookNameQueryParam: Symbol = 'book_name
  }

  object Path {
    val Books: String = "books"
    val Users: String = "users"
    val Rentals: String = "rentals"
    val Id = "id"
    val Name = "name"
    val Owner = "owner"
    val Tag = "tag"
  }

  object PathParam {
    val AnyString: Regex = ".*".r
    val UUID: Regex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r
    val StatusAction: Map[String, Int] = Map("approve" -> 1, "reject" -> -1)
  }

  val log = logger(this.getClass)

  lazy val routes: Route = {

    path(Books / Id / PathParam.AnyString) { bookId =>
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
        put { decodeRequest { entity(as[Book]) { book =>
          val updateBook = Database[Book].update(book)
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
    path(Books / Tag / PathParam.AnyString) { tag =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getBooksWithTag = Database[Book].collect(Predicate.AllBooksWithTag(tag))
          onCompleteWithBreaker(circuitBreaker)(getBooksWithTag.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    } ~
    path(Books) {
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          parameter(QueryParam.OnlyAvailableQueryParam.as[Boolean] ?) { onlyAvailable =>
              // TODO: query lending table to determine availability of the book
            val predicate = BookPredicate(isAvailable = onlyAvailable)
            val getBooks = Database[Book].collect(predicate)
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
        } ~
        post { decodeRequest { entity(as[Book]) { book =>
          val saveBook = Database[Book].save(book)
          onCompleteWithBreaker(circuitBreaker)(saveBook.unsafeToFuture) {
            case Success(Right(res)) => complete(201 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}}
      }
    } ~
    path(Users / Id / PathParam.AnyString / Books) { userId =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getBooksOfUser = Database[Book].collect(Predicate.AllBooksOwnedByUserId(userId))
          onCompleteWithBreaker(circuitBreaker)(getBooksOfUser.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    } ~
    path(Users / Id / PathParam.AnyString / Books / Tag / PathParam.AnyString) { (userId, tag) =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getBooksOfUser = Database[Book].collect(Predicate.AllBooksOwnedByUserIdWithTag(userId)(tag))
          onCompleteWithBreaker(circuitBreaker)(getBooksOfUser.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    } ~
    path(Users / Id / PathParam.AnyString) { userId =>
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
        put { decodeRequest { entity(as[WeChatUser]) { user =>
          val updateUser = Database[WeChatUser].update(user)
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
          val getAllUsers: Result[Seq[WeChatUser]] = Database[WeChatUser].collect(Predicate.AllWechatUsers)
          onCompleteWithBreaker(circuitBreaker)(getAllUsers.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        } ~
        post { decodeRequest { entity(as[WeChatUser]) { user =>
          val saveUser = Database[WeChatUser].save(user)
          onCompleteWithBreaker(circuitBreaker)(saveUser.unsafeToFuture) {
            case Success(Right(res)) => complete(201 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}}
      }
    } ~
    path(Rentals) {
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getAllRentals = Database[Rental].collect(Predicate.AllRentals)
          onCompleteWithBreaker(circuitBreaker)(getAllRentals.unsafeToFuture) {
            case Success(Right(res)) => complete(201 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        } ~
        post { decodeRequest { entity(as[Rental]) { rental =>
          val saveRental = Database[Rental].save(rental)
          onCompleteWithBreaker(circuitBreaker)(saveRental.unsafeToFuture) {
            case Success(Right(res)) => complete(201 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }}}
      }
    } ~
    path(Rentals / Id / PathParam.UUID) { rentalId =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        get {
          val getRentalById = Database[Rental].getById(rentalId)
          onCompleteWithBreaker(circuitBreaker)(getRentalById.unsafeToFuture) {
            case Success(Right(Some(rental))) => complete(200 -> rental)
            case Success(Right(None))         => complete(404 -> s"rental id $rentalId not found")
            case Success(Left(error))         => complete(500 -> error)
            case Failure(error)               => complete(500 -> error)
          }
        }
      }
    } ~
    path(Rentals / Id / PathParam.UUID / PathParam.StatusAction) { (rentalId, newStatus) =>
      headerValueByName("Authorization") { token => //TODO: implement authentication
        patch {
          val updateRentalStatus = Database[Rental].update(Rental.statusChange(rentalId, newStatus))
          onCompleteWithBreaker(circuitBreaker)(updateRentalStatus.unsafeToFuture) {
            case Success(Right(res)) => complete(200 -> res)
            case Success(Left(err))  => complete(500 -> err)
            case Failure(err)        => complete(500 -> err)
          }
        }
      }
    }
  }
}
