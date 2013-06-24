package controllers

import play.api.libs.openid._
import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}
import scala.concurrent.Future
import models.Target
import se.radley.plugin.salat.Binders.ObjectId


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("かんぱるー！"))
  }

  def list = Action {
   Ok(views.html.index("list page"))
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
      OpenID.verifiedId.map((info: UserInfo) =>
        //Ok(info.id + "\n" + info.attributes)).
        Ok(views.html.index(
            info.attributes("last") + "(" + info.attributes("email") + ")さん かんぱるへようこそ！")))
         fallbackTo(Future(Forbidden))
      )
  }

}
