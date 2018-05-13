package org.youshuren.persistence

import org.youshuren._
import org.youshuren.model.{Book, WeChatUser}

trait Database[A] { this: DatabaseImpl =>

  def post(a: A): Result[String]
  def getById(id: String): Result[Option[A]]
  def getByName(name: String): Result[Option[A]]
  def collect(predicate: => Boolean): Result[Seq[A]]
  def put(a: A): Result[String]
  def deleteById(id: String): Result[String]
}

object Database {

  def apply[A : Database]: Database[A] = implicitly[Database[A]]

  implicit val userDatabase: Database[WeChatUser] = new Database[WeChatUser] with Postgres { //TODO make generic to User

    override def collect(predicate: => Boolean): Result[Seq[WeChatUser]] = getAllWechatUsers

    override def post(a: WeChatUser): Result[String] = insert(a) map { _ map { n => s"$n users created" } }

    override def getById(id: String): Result[Option[WeChatUser]] = getWeChatUserById(id) map { _ map { _.headOption } }

    override def getByName(name: String): Result[Option[WeChatUser]] = getUserByNickName(name) map { _ map { _.headOption } }

    override def put(a: WeChatUser): Result[String] = updateWechatUser(a) map { _ map { n => s"$n users updated" } }

    override def deleteById(id: String): Result[String] = deleteUser(id)
  }

  implicit val bookDatabase: Database[Book] = new Database[Book] with Postgres {

    override def collect(predicate: => Boolean): Result[Seq[Book]] = getAllBooks(predicate)

    override def post(book: Book): Result[String] = insert(book) map { _ map { n => s"$n books created" } }

    override def getById(id: String): Result[Option[Book]] = getBookById(id) map { _ map { _.headOption } }

    override def getByName(name: String): Result[Option[Book]] = getBookByName(name) map { _ map { _.headOption } }

    override def put(book: Book): Result[String] = updateBook(book) map { _ map { n => s"$n books updated" } }

    override def deleteById(id: String): Result[String] = deleteBook(id) map { _ map { n => s"$n books deleted" } }
  }
}
