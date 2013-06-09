package models

import scala.slick.driver.H2Driver.simple._
import scala.reflect.runtime.{ universe => ru }
import shapeless._
import HList._

case class Item(
  id:Option[Long],
  name: String,
  supID: Long,
  price: Long,
  sales: Int,
  total: Int)

// Definition of the ITEMS table
object Items extends Table[Item]("ITEMS") {

  def id = column[Long]("ID", O.PrimaryKey, O AutoInc) // This is the primary key column
  def name = column[String]("ITEM_NAME")
  def supID = column[Long]("SUP_ID")
  def price = column[Long]("PRICE")
  def sales = column[Int]("SALES")
  def total = column[Int]("TOTAL")

  def * = id.? ~ name ~ supID ~ price ~ sales ~ total <> (Item.apply _, Item.unapply _)
  //def autoInc = id.? ~ name ~ supID ~ price ~ sales ~ total <> (Item, Item.unapply _) returning id

  def findAll(filter: String = "%") = {
    for {
      c <- Items
      if (c.name like ("%" + filter))
    } yield (c)
  }

  val mirror = ru.runtimeMirror(getClass.getClassLoader)

  val fields = {
    val members = ru.typeOf[Item].members.filter(m => m.isTerm && !m.isMethod).toList
    val result = members.map(_.name.decoded.trim).reverse.toVector
    println("Fields of Supplier class: " + result)
    result
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%") = {

    val sortField: String = fields(orderBy.abs - 1)
    println("The field to sort against is: " + sortField)

    // Need to give the sorting field at compile time... is there a better way ?
    val methodFields = sortField match {
      case "name" => ru.typeOf[Items.type].declaration(ru.newTermName("name")).asMethod
      case "supID" => ru.typeOf[Items.type].declaration(ru.newTermName("supID")).asMethod
      case "price" => ru.typeOf[Items.type].declaration(ru.newTermName("price")).asMethod
      case "sales" => ru.typeOf[Items.type].declaration(ru.newTermName("sales")).asMethod
      case "total" => ru.typeOf[Items.type].declaration(ru.newTermName("total")).asMethod
      case "id" => ru.typeOf[Items.type].declaration(ru.newTermName("id")).asMethod
    }

    findAll().sortBy { x =>
      val reflectedMethod = mirror.reflect(x).reflectMethod(methodFields)().asInstanceOf[Column[Any]]
      if (orderBy >= 0) reflectedMethod.asc
      else reflectedMethod.desc
    }.drop(page * pageSize).take(pageSize)
  }

  def findByPK(pk: Long) =
    for (c <- Items if c.id === pk) yield c

}


