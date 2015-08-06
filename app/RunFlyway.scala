import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion

import scala.io.StdIn

object RunFlyway {
  lazy val config = ConfigFactory.load
  lazy val dbUrl = config.getString("db.default.url")
  lazy val flyway = {
    val fw = new Flyway
    val user = config.getString("db.default.username")
    val pw = config.getString("db.default.password")
    fw.setDataSource(dbUrl, user, pw)
    fw.setBaselineOnMigrate(true)
    fw.setBaselineVersion(MigrationVersion.fromVersion("0"))
    fw
  }

  def showChanges(f: => Unit) = {
    flyway
    println(Console.BLUE + " === BEFORE ===" + Console.RESET)
    showInfo()
    try f
    finally {
      println(Console.BLUE + " === AFTER ===" + Console.RESET)
      showInfo()
    }
  }

  def main(args: Array[String]): Unit = args match {
    case Array("migrate") =>
      showChanges {
        flyway.migrate()
      }

    case Array("migrate", version) =>
      showChanges {
        flyway.setTargetAsString(version)
        flyway.migrate()
      }

    case Array("baseline", version) =>
      showChanges {
        flyway.setBaselineVersionAsString(version)
        flyway.baseline()
      }

    case Array("clean") =>
      showChanges {
        println("*** THIS WILL WIPE OUT THE ENTIRE DATABASE!!! *** ")
        println(s"Database URL: $dbUrl")
        if ("YES" == StdIn.readLine("Are you sure?? To confirm please enter YES: "))
          flyway.clean()
      }

    case Array("repair") =>
      showChanges {
        flyway.repair()
      }

    case Array("info") =>
      showInfo()

    case _ =>
      Console.err.println(
        """Usage:
          |    RunFlyway migrate [version]   # migrate to <version> or latest
          |    RunFlyway baseline <version>  # baseline to <version>
          |    RunFlyway clean               # DROP ALL DATABASE OBJECTS!!
          |    RunFlyway repair              # fix wrong checksums
          |    RunFlyway info                # print the status of all migrations
        """.stripMargin)
      sys.exit(1)
  }

  def showInfo(): Unit = {
    val infos = flyway.info().all.toSeq.map { i =>
      Seq(
        i.getVersion.toString,
        i.getDescription,
        Option(i.getInstalledOn).fold("")(_.toString),
        i.getState.getDisplayName
      )
    }
    val header = Seq("Version", "Description", "Installed on", "State")
    val widths = (header +: infos).transpose.map(_.map(_.length).max)
    def printRow(r: Seq[String], color: String = Console.RESET) = {
      print("| ")
      for ((c, w) <- r zip widths)
        print(color + c.padTo(w, ' ') + Console.RESET + " | ")
      println()
    }
    def printLine() = println(widths.map("-" * _).mkString("+-", "-+-", "-+"))
    printLine()
    printRow(header, Console.WHITE)
    printLine()
    infos foreach (printRow(_))
    printLine()
  }
}
