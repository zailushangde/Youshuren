package org.youshuren.database

import doobie._
import doobie.implicits._
import cats.effect.IO
import org.youshuren.config.AppConfig.DBConfig._
import org.youshuren.model._

trait DatabaseImpl

trait Postgres extends DatabaseImpl {

  import Postgres._

  def insert(user: User): IO[Either[Throwable, String]] = user match {
    case WeChatUser(id, nickName) =>
      sql"insert into users values ($id, $nickName)"
        .update.run.transact(transactor).attempt map { _ map { _ => id } }
    case WeGroup(id, nickName)    =>
      sql"insert into users values ($id, $nickName)"
        .update.run.transact(transactor).attempt map { _ map { _ => id } }
  }

  def insert(artifact: Artifact): IO[Either[Throwable, String]] = artifact match {
    case Book(id, name, owner, isAvailable) =>
      sql"insert into books values ($id, $name, $owner, $isAvailable)"
        .update.run.transact(transactor).attempt map { _ map { _ => id } }
  }

  def getUserById(id: String): IO[Vector[WeChatUser]] = //TODO: use User for polymorphism
    sql"SELECT * FROM users WHERE id = $id".query[WeChatUser].to[Vector].transact(transactor)

  def getUserByNickName(nickName: String): IO[Vector[WeChatUser]] = //TODO: use User for polymorphism
    sql"SELECT * FROM users WHERE nickName = $nickName".query[WeChatUser].to[Vector].transact(transactor)

  def getBookById(id: String): IO[Vector[Book]] =
    sql"SELECT * FROM books WHERE id = $id".query[Book].to[Vector].transact(transactor)

  def getBookByName(name: String): IO[Vector[Book]] =
    sql"SELECT * FROM books WHERE name = $name".query[Book].to[Vector].transact(transactor)

  def getAllBooks(onlyAvailable: Boolean = false): IO[Vector[Book]] = {
    val query =
      if (onlyAvailable) sql"SELECT * FROM books WHERE isAvailable = TRUE"
      else sql"SELECT * FROM books"
    query.query[Book].to[Vector].transact(transactor)
  }
}

object Postgres {

  protected lazy val transactor = Transactor.fromDriverManager[IO](
    driver,   // driver classname
    url,      // connect URL (driver-specific)
    username, // user
    password  // password
  )

  private val createUsersTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS users (
        id       VARCHAR NOT NULL UNIQUE,
        nickName VARCHAR
      )
      """.update

  private val createBooksTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS books (
        id    VARCHAR NOT NULL UNIQUE,
        name  VARCHAR NOT NULL,
        owner VARCHAR,
        isAvailable BOOLEAN NOT NULL
      )
      """.update

  createUsersTable.run.transact(transactor).unsafeRunSync
  createBooksTable.run.transact(transactor).unsafeRunSync
}