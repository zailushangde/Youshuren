package org.youshuren.model

sealed trait Artifact {
  def id: String
  def name: String
  def owner: String
}

case class Book(id: String, name: String, owner: String) extends Artifact
