import play.api.GlobalSettings

import models._
import play.api.db.DB
import play.api.Application
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._
// import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession


object Global extends GlobalSettings {

  override def onStart(app: Application) {
  }
}
