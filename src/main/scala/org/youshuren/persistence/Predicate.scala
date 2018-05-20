package org.youshuren.persistence

sealed trait Predicate

final case class WechatUserPredicate(id: Option[String] = None,
                                     nickName: Option[String] = None,
                                     groupId: Option[String] = None) extends Predicate

final case class BookPredicate(id: Option[String] = None,
                               name: Option[String] = None,
                               owner: Option[String] = None,
                               isAvailable: Option[Boolean] = None,
                               tag: Option[String] = None) extends Predicate

final case class RentalPredicate(id: Option[String] = None,
                                 artifactId: Option[String] = None,
                                 ownerId: Option[String] = None,
                                 borrowerId: Option[String] = None,
                                 startTime: Option[Long] = None,
                                 endTime: Option[Long] = None,
                                 status: Option[Int] = None) extends Predicate

final case class IllegalPredicateException(msg: String) extends Exception


object Predicate {

  val AllWechatUsers: WechatUserPredicate = WechatUserPredicate()

  val AllBooks: BookPredicate = BookPredicate()
  val AvailableBooks: BookPredicate = BookPredicate(isAvailable = Some(true))
  val AllBooksOwnedByUserId: String => BookPredicate = ownerId => BookPredicate(owner = Some(ownerId))
  val AllBooksWithTag: String => BookPredicate = tag => BookPredicate(tag = Some(tag))
  val AllBooksOwnedByUserIdWithTag: String => String => BookPredicate =
    ownerId => tag => BookPredicate(owner = Some(ownerId), tag = Some(tag))

  val AllRentals: RentalPredicate = RentalPredicate()
  val RentalOfId: String => RentalPredicate = id => RentalPredicate(id = Some(id))
}