package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._

import play.api.db.DB
import play.api.Play.current

// Use H2Driver to connect to an H2 database
import scala.slick.driver.H2Driver.simple._

// Use the implicit threadLocalSession
import Database.threadLocalSession

import shapeless.{Iso, ::, HList, HNil}

object ItemsController extends Controller {

  lazy val database = Database.forDataSource(DB.getDataSource())

  val pageSize = 3

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.ItemsController.list(0, 2, ""))


  /**
   * Describe the form (used in both edit and create screens).
   */
  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "supID" -> longNumber,
      "price" -> longNumber,
      "sales" -> number,
      "total" -> number)(Item.apply)(Item.unapply))

  // -- Actions

  /**
   * Handle default path requests, redirect to entities list
   */
  def index = Action { Home }

  /**
   * Display the paginated list.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on entity names
   */
  def list(page: Int, orderBy: Int, filter: String = "%") = Action { implicit request =>
    database withSession {
      Ok(html.items.list(
        Page(Items.list(page, pageSize, orderBy, filter).list,
            page,
            offset = pageSize * page,
            Items.findAll(filter).list.size),
        orderBy,
        filter))
    }
  }

  /**
   * Display an existing entity.
   *
   * @param id Id of the entity to show
   */
  def show(pk: Long) = Action {
    database withSession {
      Items.findByPK(pk).list.headOption match {
        case Some(e) => Ok(html.items.show(e))
        case None => NotFound
      }
    }
  }

  /**
   * Display the 'new form'.
   */
  def create = Action {
    database withSession {
      Ok(html.items.createForm(form))
    }
  }

   /**
   * Handle the 'new form' submission.
   */
  def save = Action { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(html.items.createForm(formWithErrors)),
      entity => {
        database withTransaction {
          Items.insert(entity)
          Home.flashing("success" -> s"Entity ${entity.name} has been created")
        }
      })
  }

  /**
   * Display the 'edit form' of an existing entity.
   *
   * @param id Id of the entity to edit
   */
  def edit(pk: Long) = Action {
    database withSession {
      Items.findByPK(pk).list.headOption match {
        case Some(e) => Ok(html.items.editForm(pk, form.fill(e)))
        case None => NotFound
      }
    }
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the entity to edit
   */
  def update(pk: Long) = Action { implicit request =>
    database withSession {
      form.bindFromRequest.fold(
        formWithErrors => BadRequest(html.items.editForm(pk, formWithErrors)),
        entity => {
          Home.flashing(Items.findByPK(pk).update(entity) match {
            case 0 => "failure" -> s"Could not update entity ${entity.name}"
            case _ => "success" -> s"Entity ${entity.name} has been updated"
          })
        })
    }
  }

  /**
   * Handle entity deletion.
   */
  def delete(pk: Long) = Action {
    database withSession {
      Home.flashing(Items.findByPK(pk).delete match {
        case 0 => "failure" -> "Entity has Not been deleted"
        case x => "success" -> s"Entity has been deleted (deleted $x row(s))"
      })
    }
  }

}

