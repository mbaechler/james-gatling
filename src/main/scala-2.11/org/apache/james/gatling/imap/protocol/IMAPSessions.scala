package org.apache.james.gatling.imap.protocol

import java.net.URI
import java.util
import java.util.Properties

import scala.collection.immutable.Seq

import akka.actor.{ActorRef, Props, Stash}
import com.lafaspot.imapnio.client.{IMAPClient, IMAPSession => ClientSession}
import com.lafaspot.imapnio.listener.IMAPConnectionListener
import com.lafaspot.logfast.logging.internal.LogPage
import com.lafaspot.logfast.logging.{LogManager, Logger}
import com.sun.mail.imap.protocol.IMAPResponse
import io.gatling.core.akka.BaseActor
import org.apache.james.gatling.imap.protocol.command.LoginHandler

case class IMAPProtocol(host: String,
                        port: Int = 143,
                        config: Properties = new Properties()
                       )

object IMAPSessions {
  def props(protocol: IMAPProtocol): Props = Props(new IMAPSessions(protocol))

  case class Connected(responses: Seq[IMAPResponse])

  case class Connect(userId: String) extends IMAPCommand

}

class IMAPSessions(protocol: IMAPProtocol) extends BaseActor {
  val imapClient = new IMAPClient(4)

  override def receive: Receive = {
    case cmd:IMAPCommand =>
      sessionFor(cmd.userId) forward cmd
  }

  private def sessionFor(userId: String) = {
    context.child(userId).getOrElse(createIMAPSession(userId))
  }

  protected def createIMAPSession(userId: String) = {
    context.actorOf(IMAPSession.props(imapClient, protocol), userId)
  }
}

private object IMAPSession {
  def props(client: IMAPClient, protocol: IMAPProtocol): Props =
    Props(new IMAPSession(client, protocol))

}

private class IMAPSession(client: IMAPClient, protocol: IMAPProtocol) extends BaseActor with Stash {
  val connectionListener = new IMAPConnectionListener {
    override def onConnect(session: ClientSession): Unit ={
      logger.error("Callback onConnect called")
      self ! IMAPSessions.Connected(Seq.empty[IMAPResponse])
    }

    override def onMessage(session: ClientSession, response: IMAPResponse): Unit =
      logger.error("Callback onMessage called")


    override def onDisconnect(session: ClientSession, cause: Throwable): Unit =
      logger.error("Callback onDisconnect called")

    override def onInactivityTimeout(session: ClientSession): Unit =
      logger.error("Callback onInactivityTimeout called")

    override def onResponse(session: ClientSession, tag: String, responses: util.List[IMAPResponse]): Unit =
      logger.error("Callback onResponse called")
  }
  val uri = new URI(s"imap://${protocol.host}:${protocol.port}")
  val config = protocol.config
  val session = client.createSession(uri, config, connectionListener, new LogManager(Logger.Level.DEBUG, LogPage.DEFAULT_SIZE))
  var tagCounter:Int=1

  override def receive: Receive = disconnected

  def disconnected: Receive = {
    case IMAPSessions.Connect(userId) =>
      logger.error(s"got connect request, connecting to $uri")
      session.connect()
      context.become(connecting(sender()))
    case msg =>
      logger.error("unexpected message " + msg)
  }

  def connecting(sender: ActorRef): Receive = {
    case msg@IMAPSessions.Connected(responses) =>
      logger.error("got connected response")
      context.become(connected)
      sender ! msg
      unstashAll()
    case x =>
      logger.error(s"got unexpected message $x")
      stash
  }

  def connected: Receive = {
    case cmd@LoginHandler.Login(_, _, _) =>
      val tag = f"A$tagCounter%06d"
      val handler = context.actorOf(LoginHandler.props(session, tag), "login")
      handler forward cmd
  }
}

