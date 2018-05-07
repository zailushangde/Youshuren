package org.youshuren.database

import org.youshuren.model.{Book, WeChatUser}

trait Database[A] { this: DatabaseImpl =>

  def post(a: A): Either[Throwable, String]
  def getById(id: String): Option[A]
  def getByName(name: String): Option[A]
  def putById(id: String, a: A): Boolean
  def putByName(name: String, a: A): Boolean
  def deleteById(id: String): Boolean
  def deleteByName(name: String): Boolean
}

object Database {

  def apply[A : Database]: Database[A] = implicitly[Database[A]]

  implicit val userDatabase = new Database[WeChatUser] with Redis {

    override def post(a: WeChatUser): Either[Throwable, String] = ???

    override def getById(id: String): Option[WeChatUser] = ???

    override def getByName(name: String): Option[WeChatUser] = ???

    override def putById(id: String, a: WeChatUser): Boolean = ???

    override def putByName(name: String, a: WeChatUser): Boolean = ???

    override def deleteById(id: String): Boolean = ???

    override def deleteByName(name: String): Boolean = ???
  }

  implicit val bookDatabase = new Database[Book] with Redis {

    override def post(a: Book): Either[Throwable, String] = ???

    override def getById(id: String): Option[Book] = ???

    override def getByName(name: String): Option[Book] = ???

    override def putById(id: String, a: Book): Boolean = ???

    override def putByName(name: String, a: Book): Boolean = ???

    override def deleteById(id: String): Boolean = ???

    override def deleteByName(name: String): Boolean = ???
  }
}
