import play.Project._

name := "shine"

version := "1.0"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "info.cukes" % "cucumber-java" % "1.2.2",
  "info.cukes" % "cucumber-junit" % "1.2.2",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.15",
  "org.skyscreamer" % "jsonassert" % "1.2.3"
)     



play.Project.playScalaSettings

templatesTypes += ("csv" -> "views.CsvFormat")