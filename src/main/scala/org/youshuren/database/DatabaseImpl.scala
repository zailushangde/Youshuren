package org.youshuren.database

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie.util.transactor.Transactor.Aux
import doobie.util.yolo
import org.youshuren.config.AppConfig._
import org.youshuren.model._
import shapeless._
import shapeless.record.Record

trait DatabaseImpl {
  /**
    * Initialize the database such as create tables, indexes, etc. Specific to what database is being used.
    */
  def setup: Unit
}

trait Postgres extends DatabaseImpl {

  private lazy val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:world", // connect URL (driver-specific)
    DBConfig.username,       // user
    DBConfig.password        // password
  )

  private lazy val y: yolo.Yolo[IO] = transactor.yolo
  import y._

  override def setup: Unit = {
    createUsersTable.run.transact(transactor).unsafeRunSync
    createBooksTable.run.transact(transactor).unsafeRunSync
  }

  private val createUsersTable =
    sql"""
      CREATE TABLE IF NOT EXISTS ${DBConfig.usersTableName} (
        id       VARCHAR NOT NULL UNIQUE,
        nickName VARCHAR
      )
      """.update

  private val createBooksTable =
    sql"""
      CREATE TABLE IF NOT EXISTS ${DBConfig.booksTableName} (
        id    VARCHAR NOT NULL UNIQUE,
        name  VARCHAR NOT NULL,
        owner VARCHAR,
        isAvailable BOOLEAN NOT NULL
      )
      """.update

  def insert(user: User) = user match {
    case WeChatUser(id, nickName) => sql"insert into ${DBConfig.usersTableName} values ($id, $nickName)".update
    case WeGroup(id, nickName)    => sql"insert into ${DBConfig.usersTableName} values ($id, $nickName)".update
  }

  def insert(artifact: Artifact) = artifact match {
    case Book(id, name, owner, isAvailable) =>
      sql"insert into ${DBConfig.booksTableName} values ($id, $name, $owner, ${isAvailable.toPostgresBoolean})".update
  }

  def getUserById(id: String): IO[Vector[WeChatUser]] = //TODO: use User for polymorphism
    sql"SELECT * FROM ${DBConfig.usersTableName} WHERE id = $id".query[WeChatUser].to[Vector].transact(transactor)

  def getBookById(id: String): IO[Vector[Book]] =
    sql"SELECT * FROM ${DBConfig.booksTableName} WHERE id = $id".query[Book].to[Vector].transact(transactor)

  def getAllBooks(onlyAvailable: Boolean = false): IO[Vector[Book]] = {
    val condition = if (onlyAvailable) s"WHERE isAvailable = TRUE" else ""
    sql"SELECT * FROM ${DBConfig.booksTableName} $condition".query[Book].to[Vector].transact(transactor)
  }

  implicit class BooleanOps(b: Boolean) {
    def toPostgresBoolean: String = if (b) "TRUE" else "FALSE"
  }
}