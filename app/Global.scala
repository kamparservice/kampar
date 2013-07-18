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

    // lazy val database = Database.forDataSource(DB.getDataSource())

    // database withSession {
    //   // Create the tables, including primary and foreign keys
    //   val ddl = (Items.ddl)

    //   ddl.drop
    //   ddl.create

    //   // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
    //   Items.insertAll(
    //     Item(Some(1),"バームクーヘン", 2, 799, 0, 0),
    //     Item(Some(2),"ローストビーフ", 3, 899, 0, 0),
    //     Item(Some(3),"おにく", 1, 999, 0, 0),
    //     Item(Some(4),"たいやき", 1, 899, 0, 0),
    //     Item(Some(5),"パンケーキ", 3, 999, 0, 0))
    // }
  }
}
