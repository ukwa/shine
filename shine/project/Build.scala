import sbt._
import Keys._
import play.Project._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

  val appName         = "shine"
  val appVersion      = "1.0.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
	javaCore,
    javaJdbc,
    javaEbean,
    cache,
    anorm,
    "org.xerial" % "sqlite-jdbc" % "3.7.2",
    "org.apache.solr" % "solr-solrj" % "4.4.0",
    "org.fluentlenium" % "fluentlenium-core" % "0.10.2",
    "org.jbehave" % "jbehave-core" % "3.9.1",
    "org.codehaus.plexus" % "plexus-archiver" % "1.2",
    "org.apache.maven.wagon" % "wagon-provider-api" % "2.6",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.jbehave" % "jbehave-maven-plugin" % "3.9.1",
    "org.apache.commons" % "commons-email" % "1.3.2",
    "commons-io" % "commons-io" % "2.3",
    "org.avaje" % "ebean" % "2.7.1",
    "net.sf.opencsv" % "opencsv" % "2.3"

  )


  val main = play.Project(appName, appVersion, appDependencies)
    .settings(Play2WarPlugin.play2WarSettings: _*)
    .settings(Play2WarKeys.servletVersion := "3.0")
    .settings(Play2WarKeys.targetName := Some(appName + "-" + appVersion))
    .settings(
    // Add your own project settings here
    // set test options
    javaOptions in Test += "-Dconfig.file=conf/application-test.conf"
  )

}
