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
import org.apache.james.gatling.imap.protocol.command.SelectHandler.{Selected, Select}

object SelectHandler{
  case class Select(userId:String, mailbox: String) extends ImapCommand
  case class Selected(response:Seq[IMAPResponse], sender:ActorRef)
  def props(session:IMAPSession,tag:String)=Props(new SelectHandler(session, tag))
}

class SelectHandler(session:IMAPSession,tag:String) extends BaseActor {

  override def receive: Receive ={
    case Select(userId, mailbox) =>
      val action = sender()
      val listener=new SelectListner(userId, logger , action)
      session.executeSelectCommand(tag, mailbox, listener)

    case msg@Selected(response, sender) =>
      sender ! msg
      context.stop(self)
  }

  class SelectListner(userId:String, logger:Logger, sender:ActorRef) extends IMAPCommandListener{
    import collection.JavaConverters._
    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response: Seq[IMAPResponse] = responses.asScala.to[Seq]
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Selected(response, sender)
    }
  }
}

