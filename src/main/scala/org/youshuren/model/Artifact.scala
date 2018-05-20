package org.youshuren.model

sealed trait Artifact {
  def id: String
  def name: String
  def owner: String
  def isAvailable: Boolean
  def description: Option[String]
  def tags: Option[List[String]]
}

case class Book(id: String,
                name: String,
                owner: String,
                isAvailable: Boolean,
                description: Option[String],
                tags: Option[List[String]]) extends Artifact
