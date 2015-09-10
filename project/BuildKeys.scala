import sbt._

object BuildKeys {
  lazy val dbConf = settingKey[(String, String, String)]("Slick connection settings")
}
