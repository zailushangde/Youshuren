package org.youshuren.model

sealed trait Artifact {
  def id: String
  def name: String
  def owner: String
  def isAvailable: Boolean
}

case class Book(id: String, name: String, owner: String, isAvailable: Boolean) extends Artifact
