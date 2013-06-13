package controllers

import play.api.libs.openid._
import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}
import scala.concurrent.Future


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("かんぱるやろーよ、はやくやろーよ、たのしいよー、うひょ！"))
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
           routes.Application.openIDCallback.absoluteURL())
          url.map(a => Redirect(a)).
           fallbackTo(Future(Redirect(routes.Application.login)))
        }
      }
    )
  }

  def openIDCallback = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId.map((info: UserInfo) =>
        Ok(info.id + "\n" + info.attributes)).
         fallbackTo(Future(Forbidden))
      )
  }

}
