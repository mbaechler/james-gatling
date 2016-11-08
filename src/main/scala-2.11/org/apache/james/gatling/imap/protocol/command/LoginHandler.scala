package org.apache.james.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor
import org.apache.james.gatling.imap.protocol.ImapCommand.Login
import org.apache.james.gatling.imap.protocol.command.LoginHandler.LoggedIn

import scala.collection.immutable.Seq

object LoginHandler{
  case class LoggedIn(response:Seq[IMAPResponse])
  def props(session:IMAPSession,tag:String)=Props(new LoginHandler(session, tag))
}

class LoginHandler(session:IMAPSession,tag:String) extends BaseActor {

  override def receive: Receive = {
    case Login(userId, user, password) =>
      val listener = new LoginListener(userId)
      session.executeLoginCommand(tag, user, password, listener)
      context.become(waitForLoggedIn(sender()))
  }

  def waitForLoggedIn(sender: ActorRef): Receive = {
    case msg@LoggedIn(response) =>
      sender ! msg
      context.stop(self)
  }

  class LoginListener(userId:String) extends IMAPCommandListener{
    import collection.JavaConverters._
    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response: Seq[IMAPResponse] = responses.asScala.to[Seq]
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! LoggedIn(response)
    }
  }
}

