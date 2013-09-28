package controllers

import play.api.libs.openid._
import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc._
import play.api.mvc.RequestHeader
import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}
import scala.concurrent.Future
import models._
import models.User
import se.radley.plugin.salat.Binders.ObjectId
import java.util.Date


object Application extends Controller {

  def index = Action { implicit request =>
    session.get("_user_openid") match {
      case Some(x) => {
        val user = User.findOneByOpenid(x).get
        Ok(views.html.index(user.username + "(" + user.email + ")さん かんぱるへようこそ！"))
      }

      case None => Ok(views.html.index("かんぱるー！"))
    }
  }

  def list = Action { implicit request =>
   Ok(views.html.index("list page")).withSession()
  }


//  def login = Action {
//    Ok(views.html.login())
//  }
  val loginForm = Form("openid" -> nonEmptyText)

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }
  def loginPost = Action { implicit request =>
    Form(single(
      "openid" -> nonEmptyText
    )).bindFromRequest.fold(
      error => {
        Logger.info("bad request " + error.toString)
        BadRequest(error.toString)
      }, {
        case (openid) => AsyncResult {
          val url = OpenID.redirectURL(openid,
           routes.Application.openIDCallback.absoluteURL(),
           Seq("email" -> "http://schema.openid.net/contact/email",
               "last" -> "http://axschema.org/namePerson/last"))
          url.map(a => Redirect(a)).
           fallbackTo(Future(Redirect(routes.Application.login)))
        }
      }
    )
  }

  def openIDCallback = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId.map((info: UserInfo) => {
        //Ok(info.id + "\n" + info.attributes)).
        println(User.findOneByOpenid(info.id))
        User.findOneByOpenid(info.id).getOrElse(
          User.save(User(openid=info.id, username=info.attributes.getOrElse("last",""), email=info.attributes.getOrElse("email",""), updated = Option(new Date())))
        )
        Redirect(routes.Application.index).withSession( session + ("_user_openid" -> info.id))}).fallbackTo(Future(Forbidden))
      )
  }

  def logout = Action { implicit request =>
     Redirect(routes.Application.index).withNewSession.flashing(
      "success" -> "You've been logged out")
  }
}
