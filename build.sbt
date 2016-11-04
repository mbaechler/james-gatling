name := "james-gatling"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

enablePlugins(GatlingPlugin)

EclipseKeys.withSource := true

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.5.9"
libraryDependencies += "io.gatling" % "gatling-test-framework" % "2.2.2"
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2"
libraryDependencies += "org.apache.commons" % "commons-email" % "1.4"
libraryDependencies += "com.github.krdev.imapnio" % "imapnio.core" % "1.0.21-linagora"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.11" % "test"

resolvers += Resolver.mavenLocal

cancelable in Global := true