import com.typesafe.config.ConfigFactory
import org.flywaydb.sbt.FlywayPlugin._

name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
lazy val migrations =
  project
  .settings(
    BuildKeys.dbConf := {
      val cfg = ConfigFactory.parseFile((resourceDirectory in (root, Compile)).value / "application.conf")
      val prefix = "slick.dbs.default.db"
      (cfg.getString(s"$prefix.url"), cfg.getString(s"$prefix.user"), cfg.getString(s"$prefix.password"))
    },
    flywaySettings,
    flywayUrl := BuildKeys.dbConf.value._1,
    flywayUser := BuildKeys.dbConf.value._2,
    flywayPassword := BuildKeys.dbConf.value._3,
    libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1200-jdbc4"
  )

scalaVersion in ThisBuild := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1200-jdbc4"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "1.0.0"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
