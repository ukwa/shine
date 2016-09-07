// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.8")

// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

// java ebean
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "1.0.0")

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
