// The Typesafe repository 
resolvers += Resolver.typesafeIvyRepo("releases")
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.6")

// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

// java ebean
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "3.0.0")

// Eclipse support
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.1.0")

// Comment to get more information during initialization
logLevel := Level.Warn

