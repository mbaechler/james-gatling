package org.apache.james.gatling.imap.protocol.command

import java.util

import akka.actor.{ActorRef, Props}
import com.lafaspot.imapnio.client.IMAPSession
import com.lafaspot.imapnio.listener.IMAPCommandListener
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor
import org.apache.james.gatling.imap.protocol.ImapCommand.Select
import org.apache.james.gatling.imap.protocol.command.SelectHandler.Selected

import scala.collection.immutable.Seq

object SelectHandler{
  case class Selected(response:Seq[IMAPResponse])
  def props(session:IMAPSession,tag:String)=Props(new SelectHandler(session, tag))
}

class SelectHandler(session:IMAPSession,tag:String) extends BaseActor {

  override def receive: Receive ={
    case Select(userId, mailbox) =>
      val listener = new SelectListener(userId)
      session.executeSelectCommand(tag, mailbox, listener)
      context.become(waitCallback(sender()))
  }

  def waitCallback(sender: ActorRef) : Receive = {
    case msg@Selected(response) =>
      sender ! msg
      context.stop(self)
  }


  class SelectListener(userId:String) extends IMAPCommandListener{
    import collection.JavaConverters._
    override def onMessage(session: IMAPSession, response: IMAPResponse): Unit = {
      logger.trace(s"Untagged message for $userId : ${response.toString}")
    }

    override def onResponse(session: IMAPSession, tag: String, responses: util.List[IMAPResponse]): Unit = {
      val response: Seq[IMAPResponse] = responses.asScala.to[Seq]
      logger.trace(s"On response for $userId :\n ${response.mkString("\n")}\n ${sender.path}")
      self ! Selected(response)
    }
  }
}

