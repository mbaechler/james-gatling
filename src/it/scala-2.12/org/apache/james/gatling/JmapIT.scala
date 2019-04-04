import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.funspec.GatlingFunSpec
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http
import org.apache.james.gatling.Fixture.{bart, homer}
import org.apache.james.gatling.{Fixture, JamesServer}
import org.apache.james.gatling.JamesServer.RunningServer
import org.apache.james.gatling.jmap.scenari.JmapAuthenticationScenario
import org.slf4j
import org.slf4j.LoggerFactory

abstract class JmapIT extends GatlingFunSpec {
  val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val server: RunningServer = JamesServer.start()
  lazy val protocolConf: Protocol = http.host("localhost").port(server.mappedImapPort()).build()
  before(server.addUser(bart))
  after(server.stop())

  protected def scenario(scenario: FeederBuilder => ScenarioBuilder) = {
    scenario(Fixture.feederBuilder(bart)).actionBuilders.reverse.foreach(
      spec _
    )
  }
}

class JmapAuthenticationScenarioIT extends JmapIT {
  scenario(new JmapAuthenticationScenario(_))
}
