package controllers

import java.util.Date
import com.mongodb.casbah.WriteConcern
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

//mail sending
import com.typesafe.plugin._

object TargetsController extends Controller {

  val pageSize = 3

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.TargetsController.list())


  /**
   * Describe the form (used in both edit and create screens).
   */
  val targetForm: Form[Target] = Form(
    // Defines a mapping that will handle Contact values
    mapping(
      "id" -> ignored(new ObjectId),
      "title" -> text,
      "price" -> optional(number),
      "image_id" -> optional(ignored(new ObjectId)),
      "updated" -> optional(date)
    )(Target.apply)(Target.unapply)
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
    Ok(views.html.targets.list(targets)(request.flash))
  }

  /**
   * Display an existing entity.
   *
   * @param id Id of the entity to show
   */
  def show(id: ObjectId) = Action {
    Target.findOneById(id).map( target =>
      Ok(views.html.targets.show(target))
    ).getOrElse(NotFound)
  }

  /**
   * Display the 'new form'.
   */
  def create = Action {
      Ok(views.html.targets.createForm(targetForm))
  }

   /**
   * Handle the 'new form' submission.
   */
  def save = Action(parse.multipartFormData) { implicit request =>
    targetForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.targets.createForm(formWithErrors)),
      target => {
        request.body.file("image") match {
          case Some(image) => {
            val gridFs = salat.gridFS("images")
            val uploadedFile = gridFs.createFile(image.ref.file)
            uploadedFile.contentType = image.contentType.orNull
            uploadedFile.save()
            val image_id = uploadedFile._id

            Target.save(Target(title = target.title, price=target.price, image_id=image_id, updated = Option(new Date())))
            
            // mail sending
            session.get("_user_openid") map { openid => {
                val user = User.findOneByOpenid(openid).get
                new Mailer("[KAMPAR]You add Target!!!", user.email, views.html.mail.addTarget.render(user).body).send()
              }
            }
            
            Home.flashing("success" -> s"Entity ${target.title} has been created")
          }
          case None => {
            Target.save(Target(title = target.title, price=None, image_id=None, updated = Option(new Date())))
            Home.flashing("success" -> s"Entity ${target.title} has been created")
          }
        }
      }
    )
  }


  /**
   * Display the 'edit form' of an existing entity.
   *
   * @param id Id of the entity to edit
   */
  def edit(id: ObjectId) = Action { implicit request =>
      Target.findOneById(id).map( target =>
        Ok(views.html.targets.editForm(id, targetForm.fill(target)))
      ).getOrElse(NotFound)
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the entity to edit
   */
  def update(id: ObjectId) = Action(parse.multipartFormData) { implicit request =>
    targetForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.targets.editForm(id, formWithErrors)),
      target => {
        val gridFs = salat.gridFS("images")
        //delte old image file
        var oldTarget = Target.findOneById(id).get
        if(oldTarget.image_id != None) {
          gridFs.remove(oldTarget.image_id.get)
        }
        request.body.file("image") match {
          case Some(image) =>
            val uploadedFile = gridFs.createFile(image.ref.file)
            uploadedFile.contentType = image.contentType.orNull
            uploadedFile.save()
            val image_id = uploadedFile._id

            Target.save(Target(id, title = target.title, price = target.price, image_id=image_id, updated = Option(new Date())))
            Home.flashing(
                "success" -> "The target has been updated",
                "target_id" -> id.toString()
            )

          case None => {
            Target.save(Target(id, title = target.title, price=None, image_id=None, updated = Option(new Date())))
            Home.flashing("success" -> s"Entity ${target.title} has been updated")
          }
        }
      }
    )
  }

  /**
   * Handle entity deletion.
   */
  def delete(id: ObjectId) = Action {
    //delte old image file
    val gridFs = salat.gridFS("images")
    Target.findOneById(id) match {
      case Some(target) => {
        if(target.image_id != None) {
          gridFs.remove(target.image_id.get)
        }
        Target.removeById(id)
        Home.flashing(
          "success" -> "The target has been deleted"
        )
      }
      case None =>
        Home.flashing(
         "failure" -> "The target is not found"
        )
    }
  }


  def image(id: ObjectId) = Action {
    import com.mongodb.casbah.Implicits._

    val gridFs = salat.gridFS("images")

    gridFs.findOne(Map("_id" -> id)) match {
      case Some(f) => SimpleResult(
        ResponseHeader(OK, Map(
          CONTENT_LENGTH -> f.length.toString,
          CONTENT_TYPE -> f.contentType.getOrElse(BINARY),
          DATE -> new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US).format(f.uploadDate)
        )),
        Enumerator.fromStream(f.inputStream)
      )

      case None => NotFound
    }
  }
}

