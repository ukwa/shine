import sbt._
import Keys._
import play.Project._

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
    "org.fluentlenium" % "fluentlenium-core" % "0.9.2",
    "org.jbehave" % "jbehave-core" % "3.9.1",
    "org.codehaus.plexus" % "plexus-archiver" % "1.2",
    "org.apache.maven.wagon" % "wagon-provider-api" % "2.6",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.jbehave" % "jbehave-maven-plugin" % "3.9.1",
	"org.apache.commons" % "commons-email" % "1.3.2",
	"commons-io" % "commons-io" % "2.3",
	"org.avaje" % "ebean" % "2.7.1"  
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
       // copy test resources
      unmanagedClasspath in Test <+= (baseDirectory) map { bd => Attributed.blank(bd / "test")}
  )

}
