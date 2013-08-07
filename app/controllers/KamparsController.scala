package controllers

import java.util.Date
import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import models._
import play.api._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import play.api.mvc.Flash
import se.radley.plugin.salat.Binders._
import se.radley.plugin._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile


object KamparsController extends Controller {

  val pageSize = 3

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.KamparsController.list())


  /**
   * Describe the form (used in both edit and create screens).
   */
  val kamparForm: Form[Kampar] = Form(
    // Defines a mapping that will handle Contact values
    mapping(
      "id" -> ignored(new ObjectId),
      "target_id" -> optional(ignored(new ObjectId)),
      "user" -> optional(text),
      "updated" -> optional(date)
    )(Kampar.apply)(Kampar.unapply)
  )



  // -- Actions

  /**
   * Handle default path requests, redirect to entities list
   */
  def index = Action { Home }

  /**
   * Display the list.
   *
   */
  def list() = Action { implicit request =>
    val targets = Target.findAll
    Ok(views.html.kampars.list(targets)(request.flash, request.session))
  }

  /**
   * Display an existing entity.
   *
   * @param id Id of the entity to show
   */
  def show(target_id: ObjectId) = Action { implicit request =>
    Target.findOneById(target_id).map( target => {
        val kampars = Kampar.find(MongoDBObject("target_id" -> target_id))
        Ok(views.html.kampars.show(target, kampars, kamparForm)(request.session))
      }
    ).getOrElse(NotFound)
  }

   /**
   * Handle the 'new form' submission.
   */
  def save(target_id: ObjectId) = Action { implicit request =>
    val target = Target.findOneById(target_id).get
    val kampars = Kampar.find(MongoDBObject("target_id" -> target_id))
    kamparForm.bindFromRequest.fold(
      formWithErrors => NotFound,
//      formWithErrors => BadRequest(views.html.kampars.show(target,kampars,formWithErrors)), //TODO 直す
      kampar => {
        session.get("_user_openid").map { _user_openid =>
          val user = User.findOneByOpenid(_user_openid).get
          Kampar.save(Kampar(target_id=Some(target_id), user = Some(user.username), updated = Option(new Date())))
          Redirect(routes.KamparsController.show(target_id)).flashing("success" -> s"${user.username} has been added")
        }.getOrElse {
          Redirect(routes.KamparsController.show(target_id)).flashing("failure" -> s"User has not been added!")
        }
      }
    )
  }

}

