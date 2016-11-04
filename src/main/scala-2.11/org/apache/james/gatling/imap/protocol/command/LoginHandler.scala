package org.apache.james.gatling.imap.protocol.command

import java.util

import scala.collection.immutable.Seq

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.sun.mail.imap.protocol.IMAPResponse
import com.typesafe.scalalogging.Logger
import io.gatling.core.akka.BaseActor
import org.apache.james.gatling.imap.protocol.ImapCommand
import org.apache.james.gatling.imap.protocol.command.LoginHandler.{LoggedIn, Login}

object LoginHandler{
  case class Login(userId:String, username:String, password:String) extends ImapCommand
  case class LoggedIn(response:Seq[IMAPResponse], sender:ActorRef)
  def props(session:IMAPSession,tag:String)=Props(new LoginHandler(session, tag))
}

class LoginHandler(session:IMAPSession,tag:String) extends BaseActor {

  override def receive: Receive ={
    case Login(userId, user, password) =>
      val action = sender()
      val listener=new LoginListner(userId, logger , action)
      session.executeLoginCommand(tag, user, password, listener)

    case msg@LoggedIn(response, sender) =>
      sender ! msg
      context.stop(self)
  }

  class LoginListner(userId:String, logger:Logger, sender:ActorRef) extends IMAPCommandListener{
    import collection.JavaConverters._
    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response: Seq[IMAPResponse] = responses.asScala.to[Seq]
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! LoggedIn(response, sender)
    }
  }
}

