package org.youshuren.database

import cats.effect.IO
import org.youshuren.model.{Book, WeChatUser}

import scala.util.Try

trait Database[A] { this: DatabaseImpl =>

  def post(a: A): IO[Either[Throwable, String]]
  def getById(id: String): IO[Option[A]]
  def getByName(name: String): IO[Option[A]]
  def collect(predicate: => Boolean): IO[Seq[A]]
  def putById(id: String, a: A): IO[Boolean]
  def putByName(name: String, a: A): IO[Boolean]
  def deleteById(id: String): IO[Boolean]
  def deleteByName(name: String): IO[Boolean]
}

object Database {

  def apply[A : Database]: Database[A] = implicitly[Database[A]]

  implicit val userDatabase: Database[WeChatUser] = new Database[WeChatUser] with Postgres {

    override def collect(predicate: => Boolean): IO[Seq[WeChatUser]] = ???

    override def post(a: WeChatUser): IO[Either[Throwable, String]] = insert(a)

    override def getById(id: String): IO[Option[WeChatUser]] = getUserById(id) map { _.headOption }

    override def getByName(name: String): IO[Option[WeChatUser]] = getUserByNickName(name) map { _.headOption }

    override def putById(id: String, a: WeChatUser): IO[Boolean] = ???

    override def putByName(name: String, a: WeChatUser): IO[Boolean] = ???

    override def deleteById(id: String): IO[Boolean] = ???

    override def deleteByName(name: String): IO[Boolean] = ???
  }

  implicit val bookDatabase: Database[Book] = new Database[Book] with Postgres {

    override def collect(predicate: => Boolean): IO[Seq[Book]] = getAllBooks(predicate)

    override def post(book: Book): IO[Either[Throwable, String]] = insert(book)

    override def getById(id: String): IO[Option[Book]] = getBookById(id) map { _.headOption }

    override def getByName(name: String): IO[Option[Book]] = getBookByName(name) map { _.headOption }

    override def putById(id: String, a: Book): IO[Boolean] = ???

    override def putByName(name: String, a: Book): IO[Boolean] = ???

    override def deleteById(id: String): IO[Boolean] = ???

    override def deleteByName(name: String): IO[Boolean] = ???
  }
}
