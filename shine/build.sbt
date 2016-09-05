name := "shine"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, PlayEbean)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  cache,
  evolutions,
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.apache.solr" % "solr-solrj" % "4.4.0",
  "org.fluentlenium" % "fluentlenium-core" % "0.10.2",
  "org.jbehave" % "jbehave-core" % "3.9.1",
  "org.codehaus.plexus" % "plexus-archiver" % "1.2",
  "org.apache.maven.wagon" % "wagon-provider-api" % "2.6",
  "org.postgresql" % "postgresql" % "9.4.1209",
  "org.jbehave" % "jbehave-maven-plugin" % "3.9.1",
  "org.apache.commons" % "commons-email" % "1.3.2",
  "commons-io" % "commons-io" % "2.3",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "info.cukes" % "cucumber-java" % "1.2.2",
  "info.cukes" % "cucumber-junit" % "1.2.2",
  "net.sourceforge.htmlunit" % "htmlunit" % "2.15",
  "org.skyscreamer" % "jsonassert" % "1.2.3",
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "org.easytesting" % "fest-assert" % "1.4" % Test,
  specs2 % Test
)

// Specify the configuration to be used during testing.
javaOptions in Test += "-Dconfig.resource=application-test.conf"

// We are using the static routes (see: https://www.playframework.com/documentation/2.5.x/Migration25#routes-generated-with-injectedroutesgenerator)
routesGenerator := StaticRoutesGenerator

fork in run := true