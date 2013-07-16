package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._
import org.bson.types.ObjectId

/**
 * かんぱる対象
 */
case class Target(
  id: ObjectId = new ObjectId,
  title: String,
  price: Option[Int],
  image_id: Option[ObjectId],
  updated: Option[Date] = None
)
//{
//  def this(id: ObjectId, title: String, image_id: String, updated: Option[Date]) = {
//    this(id, title, Some(new ObjectId(image_id)), updated)
//  }
//}

object Target extends TargetDAO

trait TargetDAO extends ModelCompanion[Target, ObjectId] {
  def collection = mongoCollection("targets")
  val dao = new SalatDAO[Target, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("title" -> 1))

  // Queries
  def findOneByTitle(title: String): Option[Target] = dao.findOne(MongoDBObject("title" -> title))
  def list() = { findAll }
}

