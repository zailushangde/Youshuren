package org.youshuren.persistence

import doobie._
import doobie.implicits._
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
    case user: WeChatUser => execute(Update.insertWeChatUser(user) run)
    case group: WeGroup   => execute(Update.insertWegroup(group) run)
  }

  def insert(artifact: Artifact): Result[Int] = artifact match {
    case book: Book => execute(Update.insertBook(book) run)
  }

  def getWeChatUserById(id: String): Result[Vector[WeChatUser]] = execute(Query.getUserById(id).to[Vector])

  def getUserByNickName(nickName: String): Result[Vector[WeChatUser]] = execute(Query.getUserByNickName(nickName).to[Vector])

  def getBookById(id: String): Result[Vector[Book]] = execute(Query.getBookById(id).to[Vector])

  def getBookByName(name: String): Result[Vector[Book]] = execute(Query.getBookByName(name).to[Vector])

  def getAllWechatUsers: Result[Vector[WeChatUser]] = execute(Query.getAllWechatUsers.to[Vector])

  def getAllBooks(onlyAvailable: Boolean = false): Result[Vector[Book]] =
    execute(if (onlyAvailable) Query.getAllBooks.to[Vector] else Query.getAllAvailableBooks.to[Vector])

  def updateWechatUser(user: WeChatUser): Result[Int] = execute(Update.updateWeChatUser(user) run)

  def updateBook(book: Book): Result[Int] = execute(Update.updateBook(book) run)

  def deleteBook(id: String): Result[Int] = execute(Update.deleteBook(id) run)

  def deleteUser(id: String): Result[String] = {
    val command: ConnectionIO[String] = for {
      weChatUsersDeleted <- Update.deleteWeChatUser(id).run
      booksUpdated       <- Update.updateBookOwner(id).run //TODO: make it generic to all artifact types
    } yield s"deleted $weChatUsersDeleted WeChat users and reset owners for $booksUpdated books"

    execute(command)
  }
}

object Postgres {

  private lazy val log = logger(this.getClass)

  private[this] var config: PostgresConfig = _

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

    val getUserById: String => Query0[WeChatUser] = userId =>
      sql"SELECT * FROM wechatusers WHERE id = $userId".query[WeChatUser] //TODO: use User for polymorphism
    val getUserByNickName: String => Query0[WeChatUser] = nickName =>
      sql"SELECT * FROM wechatusers WHERE nickName = $nickName".query[WeChatUser] //TODO: use User for polymorphism
    val getBookById: String => Query0[Book] = bookId =>
      sql"SELECT * FROM books WHERE id = $bookId".query[Book]
    val getBookByName: String => Query0[Book] = bookName =>
      sql"SELECT * FROM books WHERE name = $bookName".query[Book]
    val getAllWechatUsers: Query0[WeChatUser] =
      sql"SELECT * FROM wechatusers".query[WeChatUser]
    val getAllBooks: Query0[Book] =
      sql"SELECT * FROM books".query[Book]
    val getAllAvailableBooks: Query0[Book] =
      sql"SELECT * FROM books WHERE isAvailable = TRUE".query[Book]
  }

  object Update {

    val insertWeChatUser: WeChatUser => Update0 = user =>
      sql"INSERT INTO wechatusers VALUES (${user.id}, ${user.nickName})".update
    val insertWegroup: WeGroup => Update0 = group =>
      sql"INSERT INTO wegroups VALUES (${group.id}, ${group.nickName})".update
    val insertBook: Book => Update0 = book =>
      sql"INSERT INTO books VALUES (${book.id}, ${book.name}, ${book.owner}, ${book.isAvailable})".update
    val updateWeChatUser: WeChatUser => Update0 = user =>
      sql"UPDATE FROM books SET name = ${user.nickName} WHERE id = ${user.id}".update
    val updateBook: Book => Update0 = book =>
      sql"UPDATE FROM books SET name = ${book.name}, owner = ${book.owner}, isAvailable = ${book.isAvailable} WHERE id = ${book.id}".update
    val deleteBook: String => Update0 = bookId =>
      sql"DELETE FROM books WHERE id = $bookId".update
    val deleteWeChatUser: String => Update0 = userId =>
      sql"DELETE FROM wechatusers WHERE id = $userId".update
    val updateBookOwner: String => Update0 = userId =>
      sql"UPDATE FROM books SET owner = '' WHERE owner = $userId".update
  }

  private[this] val createUsersTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS wechatusers (
        id       VARCHAR NOT NULL UNIQUE,
        nickName VARCHAR
      )
      """.update

  private[this] val createBooksTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS books (
        id    VARCHAR NOT NULL UNIQUE,
        name  VARCHAR NOT NULL,
        owner VARCHAR,
        isAvailable BOOLEAN NOT NULL
      )
      """.update

  def start(postgresConfig: PostgresConfig): Either[Throwable, String] = {
    config = postgresConfig
    val init: ConnectionIO[String] = for {
      _ <- createUsersTable.run
      _ =  log.info("{} table initialized.", config.tables.wechatUsers)
      _ <- createBooksTable.run
      _ =  log.info("{} table initialized.", config.tables.books)
    } yield "Postgres started"

    execute(init).unsafeRunSync
  }
}