package org.apache.james.gatling.jmap.scenari

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import org.apache.james.gatling.control.User
import org.apache.james.gatling.jmap.RetryAuthentication._
import org.apache.james.gatling.jmap.{CommonSteps, JmapMessages}

import scala.concurrent.Future
import scala.concurrent.duration._

class JmapGetMessagesScenario extends Simulation {

  def generate(duration: Duration, users: Seq[Future[User]], randomlySentMails: Int): ScenarioBuilder =
    scenario("JmapGetMessages")
      .exec(CommonSteps.provisionUsersWithMessageList(users, randomlySentMails))
      .during(duration) {
        execWithRetryAuthentication(JmapMessages.getRandomMessage(), JmapMessages.getRandomMessageChecks)
          .pause(1 second , 2 seconds)
      }

}
