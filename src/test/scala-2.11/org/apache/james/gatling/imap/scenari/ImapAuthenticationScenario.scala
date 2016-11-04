package org.apache.james.gatling.imap.scenari

import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.scenario.Simulation
import org.apache.james.gatling.imap.PreDef._

import scala.concurrent.duration._
class ImapAuthenticationScenario extends Simulation {
  type Feeder = Array[Map[String,String]]
  val feeder = Array(Map("username"->"user1", "password"->"password")).circular
  val UserCount: Int = 100

  val scn = scenario("ImapAuthentication").feed(feeder)
    .exec(imap("Connect").connect()).exitHereIfFailed
    .exec(imap("login").login("${username}","${password}"))

  setUp(scn.inject(constantUsersPerSec(1000).during(60.seconds))).protocols(imap.host("192.168.1.13"))
}
object Engine extends App{
  // This sets the class for the simulation we want to run.
  val simClass = classOf[ImapAuthenticationScenario].getName

  val props = new GatlingPropertiesBuilder
  props.sourcesDirectory("./src/main/scala")
  props.binariesDirectory("./target/scala-2.11/classes")
  props.simulationClass(simClass)

  Gatling.fromMap(props.build)

}