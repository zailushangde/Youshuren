package org.youshuren.persistence

import cats.effect.IO
import org.youshuren._
import org.youshuren.model.{Book, Rental, WeChatUser}

trait Database[A] { this: DatabaseImpl =>

  def save(a: A): Result[String]
  def getById(id: String): Result[Option[A]]
  def collect(predicate: Predicate): Result[Seq[A]]
  def update(a: A): Result[String]
  def deleteById(id: String): Result[String]
}

object Database {

  def apply[A : Database]: Database[A] = implicitly[Database[A]]

  implicit val userDatabase: Database[WeChatUser] = new Database[WeChatUser] with Postgres { //TODO make generic to User

    override def collect(predicate: Predicate): Result[Seq[WeChatUser]] = getAllWechatUsers

    override def save(user: WeChatUser): Result[String] = insert(user) map { _ map { n => s"$n users created" } }

    override def getById(id: String): Result[Option[WeChatUser]] = getWeChatUserById(id) map { _ map { _.headOption } }

    override def update(a: WeChatUser): Result[String] = updateWechatUser(a) map { _ map { n => s"$n users updated" } }

    override def deleteById(id: String): Result[String] = deleteUser(id)
  }

  implicit val bookDatabase: Database[Book] = new Database[Book] with Postgres {

    override def collect(predicate: Predicate): Result[Seq[Book]] = getBooksBy(predicate)

    override def save(book: Book): Result[String] = insert(book) map { _ map { n => s"$n books created" } }

    override def getById(id: String): Result[Option[Book]] = getBookById(id) map { _ map { _.headOption } }

    override def update(book: Book): Result[String] = updateBook(book) map { _ map { n => s"$n books updated" } }

    override def deleteById(id: String): Result[String] = deleteBook(id) map { _ map { n => s"$n books deleted" } }
  }

  implicit val rentalDatabase: Database[Rental] = new Database[Rental] with Postgres {

    override def save(a: Rental): Result[String] = insert(a) map { _ map { n => s"$n rentals created" } }

    override def update(a: Rental): Result[String] = updateRental(a) map { _ map { _ => s"rental ${a.id} updated" } }

    override def getById(id: String): Result[Option[Rental]] = getRentalsBy(Predicate.RentalOfId(id)) map { _ map { _.headOption } }

    override def deleteById(id: String): Result[String] = IO.raiseError(new UnsupportedOperationException("deletion not supported")).attempt

    override def collect(predicate: Predicate): Result[Seq[Rental]] = getRentalsBy(predicate)
  }
}
