name := "james-gatling"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

enablePlugins(GatlingPlugin)

EclipseKeys.withSource := true

libraryDependencies += "io.gatling" % "gatling-test-framework" % "2.2.2" exclude("io.gatling", "gatling-http")
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" exclude("io.gatling", "gatling-http")
libraryDependencies += "org.apache.commons" % "commons-email" % "1.4"
libraryDependencies += "com.github.krdev.imapnio" % "imapnio.core" % "1.0.21-linagora"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.11" % "test"
libraryDependencies += "io.netty" % "netty-codec-http" % "4.1.5.Final"
libraryDependencies += "io.netty" % "netty-transport-native-epoll" % "4.1.5.Final"

resolvers += Resolver.mavenLocal

cancelable in Global := true
