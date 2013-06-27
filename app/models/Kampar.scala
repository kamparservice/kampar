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
 * かんぱ
 */
case class Kampar(
  id: ObjectId = new ObjectId,
  target_id: Option[ObjectId],
  user: String,
  updated: Option[Date] = None
)

object Kampar extends KamparDAO

trait KamparDAO extends ModelCompanion[Kampar, ObjectId] {
  def collection = mongoCollection("kampars")
  val dao = new SalatDAO[Kampar, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("target_id" -> 1))

  // Queries
  def findOneByTargetId(id: ObjectId): Option[Kampar] = dao.findOne(MongoDBObject("target_id" -> id))
  def list() = { findAll }
}

