import sbt._
import Keys._
import play.Project._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object ApplicationBuild extends Build {

  val appName         = "kampar"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "com.typesafe.slick" % "slick_2.10" % "1.0.0",
    "org.slf4j" % "slf4j-api" % "1.7.3",
    "com.h2database" % "h2" % "1.3.166",
    "org.xerial" % "sqlite-jdbc" % "3.6.20",
    "org.apache.derby" % "derby" % "10.6.1.0",
    "org.hsqldb" % "hsqldb" % "2.0.0",
    "postgresql" % "postgresql" % "8.4-701.jdbc4",
    "mysql" % "mysql-connector-java" % "5.1.13",
    "com.chuusai" %% "shapeless" % "1.2.4",
    "se.radley" %% "play-plugins-salat" % "1.3.0",
    "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
    )

  val main = play.Project(appName, appVersion, appDependencies).settings(defaultScalaSettings: _*).settings(
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
    resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    EclipseKeys.withSource := true,
    testOptions in Test += Tests.Argument("junitxml", "console"),
    scalacOptions += "-feature"
  )

}
