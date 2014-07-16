import play.Project._

name := "shine"

version := "1.0"

libraryDependencies ++= Seq(jdbc, anorm)

play.Project.playScalaSettings

templatesTypes += ("csv" -> "views.CsvFormat")