package org.youshuren.model

case class Rental(id: String,
                  artifactId: String,
                  ownerId: String,
                  borrowerId: String,
                  startTime: Long,
                  endTime: Long,
                  status: Int)

object Rental {

  object Status {
    val Pending  = 0
    val Approved = 1
    val Rejected = -1
  }

  def statusChange(id: String, status: Int) = Rental(id, "", "", "", 0L, 0L, status)
}

