package org.youshuren.persistence

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import Fragments._
import doobie.hikari._
import doobie.hikari.implicits._
import cats.effect.IO
import org.youshuren.config.PostgresConfig
import org.youshuren.model._
import org.youshuren._
import scala.language.higherKinds

trait DatabaseImpl

trait Postgres extends DatabaseImpl {

  import Postgres._

  def insert(user: User): Result[Int] = user match {
    case user: WeChatUser => execute(Query.insertWechatUser(user).update.run)
    case group: WeGroup   => IO.raiseError(new UnsupportedOperationException("Wechat group feature is TBD")).attempt
  }

  def insert(artifact: Artifact): Result[Int] = artifact match {
    case book: Book => execute(Query.insertBook(book).update.run)
  }

  def insert(rental: Rental): Result[Int] = execute(Query.insertRental(rental).update.run)

  def getWeChatUserById(id: String): Result[Vector[WeChatUser]] = execute(Query.selectUserById(id).query[WeChatUser].to[Vector])

  def getUserByNickName(nickName: String): Result[Vector[WeChatUser]] = execute(Query.selectUserByNickName(nickName).query[WeChatUser].to[Vector])

  def getBookById(id: String): Result[Vector[Book]] = execute(Query.selectBookById(id).query[Book].to[Vector])

  def getAllWechatUsers: Result[Vector[WeChatUser]] = execute(Query.selectAllWechatUsers.query[WeChatUser].to[Vector])

  def getBooksBy(predicate: Predicate): Result[Vector[Book]] = predicate match {
      
    case BookPredicate(id, name, owner, isAvailable, tag) =>

      val statement: Fragment = Query.selectAllBooks ++ whereAndOpt(
        id map Query.byId,
        name map Query.byName,
        owner map Query.byOwner,
        isAvailable map Query.byAvailability,
        tag map { t => fr"$t = ANY(tags)" } //TODO: how to select where a book contains at least 1 tag in given tags in query?
      )

      execute(
        statement.query[Book].to[Vector]
      )

    case _ => IO.raiseError(IllegalPredicateException("expected instance of type BookPredicate")).attempt
  }

  def getRentalsBy(predicate: Predicate): Result[Vector[Rental]] = predicate match {

    case RentalPredicate(id, artifactId, ownerId, borrowerId, startTime, endTime, status) =>

      val statement: Fragment = Query.selectAllRentals ++ whereAndOpt(
        id map Query.byId,
        artifactId map Query.byArtifactId,
        ownerId map Query.byOwnerId,
        borrowerId map Query.byBorrowerId,
        startTime map Query.byStartTime,
        endTime map Query.byEndTime,
        status map Query.byStatus
        //        tags map { tags => in(fr"tags", tags) } //TODO: how to select where a book contains at least 1 tag in given tags in query?
      )

      execute(
        statement.query[Rental].to[Vector]
      )

    case _ => IO.raiseError(IllegalPredicateException("expected instance of type RentalPredicate")).attempt
  }

  def updateWechatUser(user: WeChatUser): Result[Int] = execute(Query.updateWeChatUser(user).update.run)

  def updateBook(book: Book): Result[Int] = execute(Query.updateBook(book).update.run)

  def updateRental(rental: Rental): Result[Int] = execute(Query.updateRentalStatus(rental.id)(rental.status).update.run)

  def deleteBook(id: String): Result[Int] = execute(Query.deleteBook(id).update.run)

  def deleteUser(id: String): Result[String] = {
    val command: ConnectionIO[String] = for {
      weChatUsersDeleted <- Query.deleteWeChatUser(id).update.run
      booksUpdated       <- Query.updateBookOwner(id).update.run //TODO: make it generic to all artifact types
    } yield s"deleted $weChatUsersDeleted WeChat users and reset owners for $booksUpdated books"

    execute(command)
  }
}

object Postgres {

  private lazy val log = logger(this.getClass)

  private[this] var config: PostgresConfig = _

  private lazy val WechatUserTable: Fragment = Fragment.const(config.tables.wechatusers)
  private lazy val BooksTable     : Fragment = Fragment.const(config.tables.books)
  private lazy val RentalsTable   : Fragment = Fragment.const(config.tables.rentals)

  protected def execute[A](update: ConnectionIO[A]): Result[A] = for {
    transactor <- HikariTransactor.newHikariTransactor[IO](
                    config.driver,
                    config.url,
                    config.username,
                    config.password
                  )
    result     <- update.transact(transactor).attempt.guarantee(transactor.shutdown)
  } yield result

  object Query {

    val selectAll: Fragment = fr"SELECT * FROM"

    val insert: Fragment = fr"INSERT INTO"

    val update: Fragment = fr"UPDATE"

    val delete: Fragment = fr"DELETE FROM"

    val doNothingOnConflictId = fr"ON CONFLICT (id) DO NOTHING"

    val byId: String => Fragment = id => fr"id = $id"
    val byNickName: String => Fragment = nickName => fr"nickName = $nickName"
    val byName: String => Fragment = name => fr"name = $name"
    val byOwner: String => Fragment = owner => fr"owner = $owner"
    val byAvailability: Boolean => Fragment = isAvailable => fr"isAvailable = $isAvailable"
    val byArtifactId: String => Fragment = artifactId => fr"artifactId = $artifactId"
    val byOwnerId: String => Fragment = ownerId => fr"ownerId = $ownerId"
    val byBorrowerId: String => Fragment = borrowerId => fr"borrowerId = $borrowerId"
    val byStartTime: Long => Fragment = startTime => fr"startTime = $startTime"
    val byEndTime: Long => Fragment = endTime => fr"endTime = $endTime"
    val byStatus: Int => Fragment = status => fr"status = $status"

    lazy val selectAllBooks: Fragment =
      selectAll ++ BooksTable

    lazy val selectAllWechatUsers: Fragment =
      selectAll ++ WechatUserTable

    lazy val selectAllRentals: Fragment =
      selectAll ++ RentalsTable

    lazy val selectUserById: String => Fragment = id =>
      selectAllWechatUsers ++ whereAnd(byId(id))

    lazy val selectBookById: String => Fragment = id =>
      selectAllBooks ++ whereAnd(byId(id))

    lazy val selectUserByNickName: String => Fragment = nickName =>
      selectAllWechatUsers ++ whereAnd(byNickName(nickName))

    lazy val insertWechatUser: WeChatUser => Fragment = user =>
      insert ++ WechatUserTable ++
        fr"VALUES (${user.id}, ${user.nickName}, ${user.groupId}, ${user.avatarUrl})" ++
        doNothingOnConflictId

    lazy val insertBook: Book => Fragment = book =>
      insert ++ BooksTable ++
        fr"VALUES (${book.id}, ${book.name}, ${book.owner}, ${book.isAvailable}, ${book.description}, ${book.tags})" ++
        doNothingOnConflictId

    lazy val insertRental: Rental => Fragment = r =>
      insert ++ RentalsTable ++
        fr"VALUES (${r.id}, ${r.artifactId}, ${r.ownerId}, ${r.borrowerId}, ${r.startTime}, ${r.endTime}, ${r.status})" ++
        doNothingOnConflictId

    lazy val updateRentalStatus: String => Int => Fragment = rentalId => status =>
      update ++ RentalsTable ++
        fr"SET status = $status" ++ whereAnd(byId(rentalId))

    lazy val updateWeChatUser: WeChatUser => Fragment = user =>
      update ++ WechatUserTable ++
        fr"SET nickName = ${user.nickName}, groupId = ${user.groupId}, avatarUrl = ${user.avatarUrl}" ++
        whereAnd(byId(user.id))

    lazy val updateBook: Book => Fragment = book =>
      update ++ BooksTable ++
        fr"SET name = ${book.name}, owner = ${book.owner}, isAvailable = ${book.isAvailable}, description = ${book.description}, tags = ${book.tags}" ++
        whereAnd(byId(book.id))

    lazy val updateBookOwner: String => Fragment = owner =>
      update ++ BooksTable ++ fr"SET owner = ''" ++ whereAnd(byId(owner))

    lazy val deleteBook: String => Fragment = id =>
      delete ++ BooksTable ++ whereAnd(byId(id))

    lazy val deleteWeChatUser: String => Fragment = id =>
      delete ++ WechatUserTable ++ whereAnd(byId(id))

  }

  private[this] val createUsersTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS wechatusers (
        id        VARCHAR NOT NULL UNIQUE,
        nickName  VARCHAR,
        groupId   VARCHAR,
        avatarUrl VARCHAR
      )
      """.update

  private[this] val createBooksTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS books (
        id          VARCHAR NOT NULL UNIQUE,
        name        VARCHAR NOT NULL,
        owner       VARCHAR NOT NULL,
        isAvailable BOOLEAN DEFAULT TRUE,
        description VARCHAR,
        tags        VARCHAR[]
      )
      """.update

  private[this] val createRentalsTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS rentals (
        id         VARCHAR NOT NULL UNIQUE,
        artifactId VARCHAR NOT NULL,
        ownerId    VARCHAR NOT NULL,
        borrowerId VARCHAR NOT NULL,
        startTime  BIGINT  NOT NULL,
        endTime    BIGINT  NOT NULL,
        status     INT     DEFAULT 0
      )
      """.update

  def start(postgresConfig: PostgresConfig): Either[Throwable, String] = {
    config = postgresConfig
    val init: ConnectionIO[String] = for {
      _ <- createUsersTable.run
      _ =  log.info("{} table initialized.", config.tables.wechatusers)
      _ <- createBooksTable.run
      _ =  log.info("{} table initialized.", config.tables.books)
      _ <- createRentalsTable.run
      _ =  log.info("{} table initialized.", config.tables.rentals)
    } yield "Postgres started"

    execute(init).unsafeRunSync
  }
}